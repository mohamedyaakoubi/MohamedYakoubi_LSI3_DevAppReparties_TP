# TP 5 : Invocation des méthodes sur des objets distants avec Java RMI

| | |
|---|---|
| **Étudiant** | Mohamed Yakoubi |
| **Classe** | LSI 3 |
| **Matière** | Développement d'applications réparties |
| **Établissement** | Faculté des Sciences de Sfax |
| **Année universitaire** | 2026 – 2027 |

---

## Objectif du TP

Avec les sockets, je devais moi-même envoyer les octets, les objets et lire les réponses. Avec **RMI** (Remote Method Invocation), le client appelle une **méthode d'un objet qui est sur le serveur**, comme si l'objet était local. Java s'occupe du réseau.

### Quelques mots importants

| Mot | Explication simple |
|---|---|
| **Interface distante** | Une interface qui hérite de `Remote`. Ses méthodes peuvent être appelées à distance et déclarent `RemoteException`. |
| **Objet distant** | L'objet qui implémente l'interface, sur le serveur (`extends UnicastRemoteObject`). |
| **RMIRegistry (annuaire)** | Un annuaire où le serveur publie l'objet sous un nom ; le client y cherche ce nom (`lookup`). Port par défaut : `1099`. |
| **Stub** | Le représentant local de l'objet distant côté client. |
| **JNDI** | Une API standard Java pour les annuaires (`InitialContext`, `rebind`, `lookup`). |

---

## Outils utilisés

| Outil | Version |
|---|---|
| Système | Windows 10 |
| Java (JDK) | OpenJDK 25 (Temurin) |
| IDE | IntelliJ IDEA |

---

## Structure du projet

```text
TP5
├── conversionClient.jar        (interface IConversion seulement)
├── banqueClient.jar            (IBanque + Compte, sans BanqueImpl)
├── screenshots/
├── RMIConversion_Server/src/
│   ├── rmiService/IConversion.java, ConversionImpl.java
│   └── rmiServer/ConversionServer.java
├── RMIConversion_Client/src/
│   └── rmiClient/ConversionClient.java
├── RMIBanque_Server/src/
│   ├── jndi.properties
│   ├── metier/Compte.java
│   ├── rmiService/IBanque.java, BanqueImpl.java
│   └── rmiServer/BanqueServer.java, BanqueServer2.java (JNDI)
└── RMIBanque_Client/src/
    ├── jndi.properties
    └── rmiClient/BanqueClient.java, BanqueClient2.java (JNDI)
```

---

## Activité 5-1 : Service de conversion de devise

### Étape 2 : Développement du serveur

**L'interface distante** (`rmiService.IConversion`) :

```java
public interface IConversion extends Remote {
    double convertirMontant(double mt) throws RemoteException;
}
```

**L'implémentation** (`rmiService.ConversionImpl`) : elle hérite de `UnicastRemoteObject` et multiplie le montant par le taux (1 € = 3,3 DT).

```java
public double convertirMontant(double mt) throws RemoteException {
    return mt * TAUX_EUR_TND;
}
```

**Le serveur** (`rmiServer.ConversionServer`) :

```java
Registry registry = LocateRegistry.createRegistry(1099);   // i.   activer l'annuaire
ConversionImpl conversion = new ConversionImpl();          // ii.  créer l'objet
registry.rebind("conversion", conversion);                 // iii. publier la référence
```

### Étape 3 : Développement du client

**Exporter l'interface dans un JAR :** le client a besoin de l'interface `IConversion`, mais **pas** de `ConversionImpl` (le code métier reste sur le serveur). J'ai créé `conversionClient.jar` avec seulement l'interface, puis je l'ai ajouté au classpath du projet client. J'ai fait la même chose pour l'activité 5-2 avec `banqueClient.jar`.

![Contenu des fichiers JAR](screenshots/jar%20files%20content.png)

**Description :** La commande `jar tf` liste le contenu des JAR. `conversionClient.jar` contient seulement `rmiService/IConversion.class`. `banqueClient.jar` contient `metier/Compte.class` et `rmiService/IBanque.class`. Les classes d'implémentation (`ConversionImpl`, `BanqueImpl`) ne sont **pas** dans les JAR.

**Le client** (`rmiClient.ConversionClient`) :

```java
Registry registry = LocateRegistry.getRegistry("localhost", 1099);
IConversion conversion = (IConversion) registry.lookup("conversion");  // ii.  chercher la référence
double resultat = conversion.convertirMontant(500);                    // iii. appel distant
```

### Étape 4 : Exécution de l'application

Je lance d'abord le serveur, puis le client.

![Serveur de conversion](screenshots/5-1%20conversion%20server.png)

**Description :** Le serveur affiche `Serveur RMI pret sur le port 1099` et reste en attente.

![Client de conversion](screenshots/5-1%20conversion%20client.png)

**Interprétation du résultat :** Le client affiche `500 EUR = 1650.0 TND`. Le calcul `500 × 3,3` a été fait **sur le serveur** : le client n'a que l'interface, il ne connaît pas le taux. Le client ajoute `..\conversionClient.jar` à son classpath pour connaître l'interface `IConversion`.

---

## Activité 5-2 : Service de banque en utilisant RMI et JNDI

### Étape 2 : Développement du serveur

**La classe `Compte`** (package `metier`) est `Serializable`, car elle est envoyée du client vers le serveur :

```java
public class Compte implements Serializable {
    private int code;
    private double solde;
    private Date dateCreation;
}
```

**L'interface distante** (`rmiService.IBanque`) :

```java
public interface IBanque extends Remote {
    String creerCompte(Compte c) throws RemoteException;
    String getInfoCompte(int code) throws RemoteException;
}
```

**L'implémentation** (`BanqueImpl`) garde les comptes dans une `Map`. Elle refuse un code qui existe déjà :

```java
if (comptes.containsKey(c.getCode())) {
    return "Erreur : le compte " + c.getCode() + " existe deja";
}
comptes.put(c.getCode(), c);
```

Le serveur `BanqueServer` publie l'objet sous le nom `banque`, comme dans l'activité 5-1.

### Étape 3 et 4 : Client et exécution

Le client crée le compte 1, essaie de le recréer, puis consulte le compte 1 et le compte 2 (qui n'existe pas).

![Serveur banque RMI](screenshots/5-2%20banque%20server.png)

**Description :** Le serveur affiche `Serveur Banque RMI pret sur le port 1099`.

![Client banque RMI](screenshots/5-2%20banque%20client.png)

**Interprétation du résultat :**

| Appel | Réponse du serveur | Explication |
|---|---|---|
| `creerCompte(new Compte(1, 1000))` | `Compte 1 cree avec succes` | Le compte 1 n'existait pas. |
| `creerCompte(new Compte(1, 500))` | `Erreur : le compte 1 existe deja` | Le code 1 est déjà utilisé, le compte n'est pas créé. |
| `getInfoCompte(1)` | `Compte 1 : solde = 1000.0, cree le ...` | Le solde est toujours `1000.0` : la deuxième création a bien été refusée. On voit aussi la date de création. |
| `getInfoCompte(2)` | `Erreur : le compte 2 n'existe pas` | Aucun compte avec ce code. |

### Étape 5 : Utilisation de JNDI

Je crée `BanqueServer2` et `BanqueClient2` (je garde les classes de l'étape 4). Le fichier `jndi.properties`, dans `src` des deux projets, indique à JNDI d'utiliser le registre RMI :

```properties
java.naming.factory.initial=com.sun.jndi.rmi.registry.RegistryContextFactory
java.naming.provider.url=rmi://localhost:1099
```

```java
// BanqueServer2 : binding avec JNDI
LocateRegistry.createRegistry(1099);
InitialContext ctx = new InitialContext();
ctx.rebind("banque", new BanqueImpl());
```

```java
// BanqueClient2 : lookup avec JNDI (au lieu de Naming.lookup / registry.lookup)
InitialContext ctx = new InitialContext();
IBanque banque = (IBanque) ctx.lookup("banque");
```

`new InitialContext()` lit automatiquement `jndi.properties` : l'adresse de l'annuaire n'est plus écrite dans le code.

![Serveur banque avec JNDI](screenshots/5-2%20banque%20server%20jndi.png)

![Client banque avec JNDI](screenshots/5-2%20banque%20client%20jndi.png)

**Interprétation du résultat :** Le serveur affiche `Serveur Banque RMI + JNDI pret sur le port 1099`. Le client affiche exactement les mêmes réponses qu'à l'étape 4. La communication RMI marche donc toujours, seule la façon d'enregistrer et de chercher l'objet a changé (JNDI).

---

## Comment lancer le projet

1. **Activité 5-1 :** lancer `rmiServer.ConversionServer`, puis `rmiClient.ConversionClient` (avec `conversionClient.jar` dans le classpath).
2. **Activité 5-2 :** lancer `rmiServer.BanqueServer`, puis `rmiClient.BanqueClient` (avec `banqueClient.jar`).
3. **Version JNDI :** lancer `rmiServer.BanqueServer2`, puis `rmiClient.BanqueClient2`.

> Tous les serveurs utilisent le port `1099` : il faut arrêter un serveur avant d'en lancer un autre.

---

## Conclusion

Dans ce TP, j'ai appris à :

- définir une interface distante (`Remote`, `RemoteException`) et l'implémenter avec `UnicastRemoteObject` ;
- publier un objet dans le RMIRegistry et le retrouver avec `lookup` ;
- partager seulement l'interface avec le client grâce à un fichier JAR ;
- envoyer un objet `Serializable` (`Compte`) en paramètre d'une méthode distante ;
- utiliser JNDI (`InitialContext`, `jndi.properties`) à la place du registre RMI direct.
