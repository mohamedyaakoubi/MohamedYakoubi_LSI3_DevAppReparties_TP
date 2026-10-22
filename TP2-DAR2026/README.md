# TP 2 : Les sockets en mode connecté TCP : E/R de CC & objets

| | |
|---|---|
| **Étudiant** | Mohamed Yakoubi |
| **Classe** | LSI 3 |
| **Matière** | Développement d'applications réparties |
| **Établissement** | Faculté des Sciences de Sfax |
| **Année universitaire** | 2026 – 2027 |

---

## Objectif du TP

Dans le TP1 (activité 1-2), le client et le serveur n'échangeaient qu'**un seul octet** (ou un seul entier). On ne pouvait donc pas envoyer une opération complète comme `55 * 25`.

Dans ce TP, j'améliore la calculatrice de deux façons :

- **Activité 2-1 :** échanger des **chaînes de caractères** (CC) avec les flux de traitement de texte ;
- **Activité 2-2 :** échanger des **objets** avec l'interface `Serializable`.

### Quelques mots importants

| Mot | Explication simple |
|---|---|
| **Flux de communication** | `InputStream` / `OutputStream` : ils transportent des octets bruts. |
| **Flux de traitement** | Des classes qui « enveloppent » un flux de communication pour lire/écrire des données plus pratiques (texte, objets…). |
| **`BufferedReader` / `PrintWriter`** | Lire et écrire du texte ligne par ligne (`readLine()`, `println()`). |
| **`ObjectInputStream` / `ObjectOutputStream`** | Lire et écrire des objets Java complets (`readObject()`, `writeObject()`). |
| **`Serializable`** | Une interface qui permet de transformer un objet en octets pour l'envoyer sur le réseau. |

---

## Outils utilisés

| Outil | Version |
|---|---|
| Système | Windows 10 |
| Java (JDK) | OpenJDK 25 (Temurin) |
| IDE | IntelliJ IDEA |
| Git | 2.45.2 |

---

## Structure du projet

```text
TP2-DAR2026
├── screenshots/
└── src/
    ├── clientPackage/
    │   ├── Client.java       (activité 2-1 : envoi de chaînes)
    │   └── Client2.java      (activité 2-2 : envoi d'un objet)
    ├── commonPackage/
    │   ├── Operation.java    (opération simple, sans Serializable)
    │   └── Operation2.java   (activité 2-2 : opération Serializable)
    └── serverPackage/
        ├── Server.java       (activité 2-1)
        └── Server2.java      (activité 2-2)
```

Comme demandé, je ne modifie pas les classes d'une activité précédente : chaque activité a ses propres classes.

---

## Activité 2-1 : Échange de CC entre Client/Serveur via les Sockets TCP

### Étape 1 : Préparation de l'espace de travail

Mon dépôt s'appelle **`MohamedYakoubi_LSI3_DevAppReparties_TP`**. Il contient un dossier par TP. Le projet de ce TP est `TP2-DAR2026`.

### Étape 2 : Application des flux de traitement de CC

J'enveloppe les flux de la socket dans des flux de **texte** :

```java
// Serveur et client : flux de traitement de texte
BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); // true = envoi immédiat
```

**Ce que j'ai fait (les sous-étapes du TP) :**

| Étape du TP | Côté | Code | Explication |
|---|---|---|---|
| a) Envoyer les éléments de l'opération | Client | `pw.println(op1)`, `pw.println(operateur)`, `pw.println(op2)` | Le client envoie 3 lignes : le premier opérande, l'opérateur, le deuxième opérande. |
| b) Calculer l'opération | Serveur | `calculer(a, operateur, b)` | Le serveur lit les 3 lignes avec `readLine()` et fait le calcul. |
| c) Envoyer le résultat | Serveur | `pw.println(reponse)` | Le serveur renvoie le résultat sous forme de texte. |
| d) Lire et afficher le résultat | Client | `br.readLine()` | Le client affiche le résultat reçu. |

**Validation de la syntaxe (NB du TP) :** avant d'envoyer, le client vérifie l'opération avec une expression régulière : un nombre, un opérateur (`+ - * /`), un nombre.

```java
static final Pattern SYNTAXE = Pattern.compile(
        "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");
```

Si la syntaxe est fausse (par exemple `25 ** 55` ou `abc + 3`), **rien n'est envoyé** au serveur.

### Lancer le serveur, puis le client

![Lancement du serveur](screenshots/server%20run.png)

**Description :** Le serveur affiche `Je suis un serveur en attente la connexion d'un client`. Il est bloqué sur `accept()`.

![Le client est connecté](screenshots/client%20connected.png)

**Description :** Le client affiche `je suis un client connecté`, puis il me demande une opération.

![Le serveur voit le client](screenshots/server%20client%20connected.png)

**Description :** Côté serveur, le message `un client est connecté` apparaît.

### Tester des opérations

![Le client envoie des opérations](screenshots/client%20sends%20operations.png)

![Le serveur calcule](screenshots/server%20calculates%20operations.png)

**Interprétation du résultat :**

| Saisie | Résultat chez le client | Côté serveur |
|---|---|---|
| `55 * 25` | `55.0 * 25.0 = 1375.0` | `Opération reçue : 55 * 25` puis la réponse envoyée |
| `25 ** 55` | `Syntaxe invalide, rien n'est envoyé.` | rien (le serveur ne reçoit rien) |
| `abc + 3` | `Syntaxe invalide, rien n'est envoyé.` | rien |
| `10 / 0` | `Erreur : division par zéro impossible` | le serveur détecte la division par zéro |
| `-7 - 3.5` | `-7.0 - 3.5 = -10.5` | les nombres négatifs et décimaux marchent |

- Le serveur ne montre que **3 opérations** : les deux saisies invalides ont été bloquées **côté client**, avant l'envoi.
- Maintenant, le client et le serveur échangent **plus d'un octet** : une opération entière et un résultat sous forme de texte.

### Fin de la communication

![Le client quitte](screenshots/client%20quit.png)

![Fin côté serveur](screenshots/server%20end%20of%20communication.png)

**Description :** Je tape `quit`. Le client envoie `quit` au serveur et affiche `Fin de la communication`. Le serveur reçoit `quit`, sort de sa boucle, affiche `Fin de la communication` et ferme ses sockets.

---

## Activité 2-2 : Échange des objets entre Client/Serveur via les Sockets TCP

### Étape 1 : Préparation de l'espace de travail

Je continue dans le même projet et j'insère les captures d'écran dans ce README.

### Étape 2 : Implémentation de l'échange d'objets

**a) Créer une classe `Serializable`**

La classe `Operation2` (dans `commonPackage`) contient les arguments de l'opération et implémente `Serializable` :

```java
public class Operation2 implements Serializable {
    private double operand1;
    private char operator;
    private double operand2;
    ...
}
```

Sans `implements Serializable`, Java refuse d'envoyer l'objet (`NotSerializableException`). C'est pour ça que j'ai gardé `Operation` (sans `Serializable`) et créé `Operation2`.

**b) à g) Envoyer l'objet et recevoir le résultat**

```java
// Client2 : envoyer l'objet, lire le résultat
outObj.writeObject(Operation2.parse(input));
Object response = inObj.readObject();
```

```java
// Server2 : recevoir l'objet, récupérer ses données, calculer, renvoyer
Operation2 op = (Operation2) inObj.readObject();
double result = op.compute();
outObj.writeObject(result);
```

| Étape du TP | Côté | Explication |
|---|---|---|
| b) Envoyer un objet | Client | Le client crée un objet `Operation2` à partir de la saisie et l'envoie avec `writeObject`. |
| c) d) Recevoir l'objet et récupérer les données | Serveur | `readObject()` reconstruit l'objet ; le serveur lit ses champs (opérandes, opérateur). |
| e) Calculer | Serveur | `op.compute()` fait le calcul. |
| f) Renvoyer le résultat | Serveur | Le résultat (`Double`) est renvoyé comme objet. |
| g) Lire et afficher | Client | Le client lit l'objet reçu et l'affiche. |

Le client vérifie aussi la saisie avant l'envoi (`Operation2.parse`).

### Lancer le serveur, puis le client

![Lancement de Server2](screenshots/server2%20run.png)

**Description :** `Server2` attend un client sur le port `12345`.

![Server2 : client connecté](screenshots/server2%20client%20connected.png)

**Description :** Le client est connecté : `Client connected: /127.0.0.1`.

### Tester l'envoi d'objets

![Client2 envoie des objets Operation2](screenshots/client2%20sends%20operation%20objects.png)

![Server2 reçoit les objets et calcule](screenshots/server2%20receives%20objects%20and%20calculates.png)

**Interprétation du résultat :**

- `55 * 25` donne `Result: 1375.0` et `100 / 8` donne `Result: 12.5`. Le serveur affiche `Computed: ...` pour chaque objet reçu.
- `4 / 0` : le serveur attrape l'erreur (`Arithmetic error: Division by zero`) et renvoie un message d'erreur (un objet `String`) au lieu d'un `Double`.
- `abc + 3` : la saisie est refusée côté client (`Invalid input`), aucun objet n'est envoyé.
- Après `exit`, le serveur affiche `Client disconnected.`

**Différence entre les deux activités :** dans l'activité 2-1, j'envoie 3 lignes de texte et le serveur doit les convertir en nombres. Dans l'activité 2-2, j'envoie **un seul objet** qui contient déjà tous les champs avec leur bon type.

---

## Comment lancer le projet

1. Ouvrir `TP2-DAR2026` dans IntelliJ IDEA.
2. **Activité 2-1 :** lancer `serverPackage.Server`, puis `clientPackage.Client` (port `1234`).
3. **Activité 2-2 :** lancer `serverPackage.Server2`, puis `clientPackage.Client2` (port `12345`).

---

## Conclusion

Dans ce TP, j'ai appris à :

- utiliser les flux de traitement de texte (`BufferedReader`, `PrintWriter`) pour échanger des chaînes ;
- valider une opération côté client avant de l'envoyer ;
- rendre une classe `Serializable` et échanger des objets avec `ObjectInputStream` / `ObjectOutputStream` ;
- gérer les erreurs (division par zéro, saisie invalide) des deux côtés.
