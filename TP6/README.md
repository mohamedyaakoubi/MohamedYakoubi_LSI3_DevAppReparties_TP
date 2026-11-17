# TP 6 : Communications client-serveur avec CORBA

## Activité : Service Banque en CORBA

| | |
|---|---|
| **Étudiant** | Mohamed Yakoubi |
| **Classe** | LSI 3 |
| **Matière** | Développement d'applications réparties |
| **Établissement** | Faculté des Sciences de Sfax |
| **Année universitaire** | 2026 – 2027 |

---

## Objectif du TP

Je crée une application répartie avec le middleware **CORBA** : un serveur gère des comptes bancaires et un client appelle ses méthodes à distance. Le serveur permet de :

- créer un compte avec une somme initiale ;
- verser et retirer de l'argent (sachant le code du compte) ;
- consulter un compte et la liste de tous les comptes ;
- convertir un montant d'euros (€) en dinars tunisiens (DT).

### Quelques mots importants

| Mot | Explication simple |
|---|---|
| **IDL** | Un langage neutre pour écrire le contrat (l'interface) entre le client et le serveur. |
| **`idlj`** | Le compilateur IDL du JDK : il génère le stub, le squelette et les classes Java à partir du `.idl`. |
| **ORB** | Le « bus » CORBA qui transporte les appels entre le client et le serveur. |
| **POA** | L'adaptateur d'objets : il transforme le servant Java en objet CORBA joignable. |
| **Servant** | La classe Java qui contient le vrai code métier (`BanqueImpl`). |
| **Naming Service** | L'annuaire CORBA (`tnameserv`, port `900`). |

---

## Outils utilisés

| Outil | Version |
|---|---|
| Système | Windows 10 |
| Java (JDK) | **JDK 8** (Temurin 1.8.0_512) |
| IDE | IntelliJ IDEA |

> CORBA (`org.omg.*`, `idlj`, `tnameserv`) a été retiré du JDK à partir de Java 11. J'utilise donc un JDK 8 pour ce TP, comme indiqué dans le cours.

---

## Étape 1 : Préparation de l'espace de travail

Le TP est dans le dossier `TP6` de mon dépôt **`MohamedYakoubi_LSI3_DevAppReparties_TP`**, avec deux projets :

```text
TP6
├── screenshots/
├── TP6_CorbaBanque_Server/src/
│   ├── Banque.idl
│   ├── jndi.properties
│   ├── corbaBanque/        (généré par idlj)
│   ├── service/BanqueImpl.java
│   └── corbaServer/BanqueServer.java
└── TP6_CorbaBanque_Client/src/
    ├── Banque.idl          (copié du serveur)
    ├── jndi.properties     (copié du serveur)
    ├── corbaBanque/        (généré par idlj)
    └── corbaClient/BanqueClient.java
```

---

## Étape 2 : Développement du serveur

### Le fichier IDL

```idl
module corbaBanque {
    struct Compte {
        long code;
        float solde;
    };
    typedef sequence<Compte> tabComptes;

    interface IBanqueRemote {
        void creerCompte(in Compte cpte);
        void verser(in float mt, in long code);
        void retirer(in float mt, in long code);
        Compte getCompte(in long code);
        tabComptes getComptes();
        double conversion(in float mt);
    };
};
```

- `module` devient un **package** Java (`corbaBanque`).
- `struct Compte` devient une classe Java avec des champs publics (`code`, `solde`).
- `typedef sequence<Compte> tabComptes` devient un tableau `Compte[]`.
- `long` en IDL devient `int` en Java ; `in` veut dire que le paramètre va du client vers le serveur.

### Compilation IDL

Dans le dossier `src` du projet serveur, je tape `idlj -fall -v Banque.idl` :

![Compilation IDL côté serveur](screenshots/idl%20compilation%20server.png)

**Description :** `java -version` montre que j'utilise le JDK `1.8.0_512`. `idlj` analyse le fichier (`Parsing Banque.idl`) puis génère le package `corbaBanque`. La commande `dir` montre les fichiers générés, notamment :

| Fichier généré | Rôle |
|---|---|
| `IBanqueRemoteOperations.java` | Les méthodes de l'interface. |
| `IBanqueRemote.java` | L'interface de l'objet distant. |
| `IBanqueRemotePOA.java` | Le **squelette** (skeleton), utilisé par le servant. |
| `_IBanqueRemoteStub.java` | La **souche** (stub), utilisée par le client. |
| `IBanqueRemoteHelper.java` | Contient `narrow()` pour convertir une référence. |
| `Compte.java`, `tabComptesHelper.java`… | Les classes de la structure et du tableau. |

### Le servant `BanqueImpl`

`BanqueImpl` hérite du squelette `IBanqueRemotePOA` et implémente toutes les méthodes. Les comptes sont gardés dans une `Map` :

```java
public class BanqueImpl extends IBanqueRemotePOA {
    private Map<Integer, Compte> comptes = new HashMap<Integer, Compte>();

    public void verser(float mt, int code) {
        Compte cpte = comptes.get(code);
        cpte.solde = cpte.solde + mt;
    }

    public double conversion(float mt) {
        return mt * 3.3;
    }
    ...
}
```

Pour `retirer`, le serveur refuse le retrait si le solde est insuffisant.

### Configuration JNDI

`jndi.properties` (dans `src`) donne l'adresse de l'annuaire CORBA :

```properties
java.naming.factory.initial=com.sun.jndi.cosnaming.CNCtxFactory
java.naming.provider.url=iiop://localhost:900
```

### Le serveur `BanqueServer`

```java
ORB orb = ORB.init(args, null);                                               // a) initialiser l'ORB
POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
poa.the_POAManager().activate();                                              // b) activer le POAManager
Context ctx = new InitialContext();                                           // c) annuaire JNDI
BanqueImpl od = new BanqueImpl();                                             // d) créer le servant
ctx.rebind("BK", poa.servant_to_reference(od));                               // e) enregistrer la référence
orb.run();                                                                    // f) attendre les clients
```

---

## Étape 3 : Développement du client

1. Je copie `Banque.idl` et `jndi.properties` dans `TP6_CorbaBanque_Client/src`.
2. Je compile de nouveau le fichier IDL dans le projet client :

![Compilation IDL côté client](screenshots/idl%20compilation%20client.png)

**Description :** Le client a maintenant son propre package `corbaBanque`, avec les mêmes fichiers. Le contrat commun entre le client et le serveur, c'est le fichier **IDL**.

3. La classe `corbaClient.BanqueClient` :

```java
Context ctx = new InitialContext();                                                 // a) propriétés JNDI
Object ref = ctx.lookup("BK");                                                      // b) référence distante
IBanqueRemote stub = IBanqueRemoteHelper.narrow((org.omg.CORBA.Object) ref);        // c) narrow -> stub
stub.creerCompte(new Compte(1, 1000));                                              // d) appels distants
stub.verser(300, 1);
stub.retirer(200, 2);
```

Il y a deux conversions : `lookup` renvoie un `java.lang.Object`, que je convertis en `org.omg.CORBA.Object`, puis `narrow` crée le stub typé `IBanqueRemote`. En CORBA, on n'utilise pas un simple cast `(IBanqueRemote)` comme en RMI.

---

## Étape 4 : Exécution de l'application

L'ordre est important : **annuaire, puis serveur, puis client**.

### 1. Lancer l'annuaire (Naming Service)

Dans le dossier du projet serveur, je tape `tnameserv` :

![Lancement de tnameserv](screenshots/tnameserv%20naming%20service.png)

**Description :** `tnameserv` affiche l'IOR (la référence CORBA du contexte de noms), puis `setting port for initial object references to: 900` et `Ready.`. L'annuaire attend sur le port `900`, le même que dans `jndi.properties`.

### 2. Lancer le serveur

![Serveur prêt](screenshots/banque%20server%20ready.png)

**Description :** Le serveur affiche `Serveur Banque CORBA pret, en attente des clients...`. Il est bloqué sur `orb.run()`.

### 3. Lancer le client

![Client CORBA](screenshots/banque%20client.png)

![Le serveur traite les requêtes](screenshots/banque%20server%20requests.png)

**Interprétation du résultat :**

| Méthode appelée par le client | Affichage du client | Affichage du serveur |
|---|---|---|
| `creerCompte` (×2) | `Comptes 1 et 2 crees` | `Compte 1 cree avec un solde de 1000.0`, `Compte 2 cree avec un solde de 500.0` |
| `verser(300, 1)` | `Versement de 300 sur le compte 1` | `nouveau solde : 1300.0` |
| `retirer(200, 2)` | `Retrait de 200 du compte 2` | `nouveau solde : 300.0` |
| `getCompte(1)` | `Compte 1 : solde = 1300.0` | `Consultation du compte 1` |
| `getComptes()` | la liste : compte 1 (1300.0) et compte 2 (300.0) | `Consultation de la liste des comptes` |
| `conversion(100)` | `100 euros = 330.0 DT` | `Conversion de 100.0 euros` |

- Toutes les méthodes de l'interface IDL ont été testées, et chacune s'affiche aussi côté serveur : le traitement est bien fait **sur le serveur**.
- `getCompte` renvoie une structure `Compte` et `getComptes` renvoie un tableau `tabComptes` : CORBA transporte aussi des types complexes.
- Je n'ai pas testé de retrait avec un solde insuffisant dans cette exécution.

---

## Comment lancer le projet (avec un JDK 8)

1. `tnameserv` (dans le dossier du projet serveur).
2. Lancer `corbaServer.BanqueServer`.
3. Lancer `corbaClient.BanqueClient`.

---

## Conclusion

Dans ce TP, j'ai appris à :

- écrire un contrat IDL (module, struct, typedef, interface) et le compiler avec `idlj` ;
- implémenter un servant qui hérite du squelette (`IBanqueRemotePOA`) ;
- initialiser l'ORB, activer le POA et publier une référence avec JNDI ;
- récupérer la référence côté client et créer le stub avec `narrow` ;
- lancer les composants dans le bon ordre : annuaire, serveur, client.

**Comparaison avec RMI (TP5) :** en RMI, le contrat est une interface Java et l'annuaire est `rmiregistry` (port `1099`). En CORBA, le contrat est un fichier IDL (indépendant du langage) et l'annuaire est `tnameserv` (port `900`).
