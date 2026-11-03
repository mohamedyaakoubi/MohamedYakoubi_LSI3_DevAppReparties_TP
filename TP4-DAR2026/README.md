# TP 4 : Sockets en mode non-connecté UDP

## Activité : Service de chat Client/Serveur avec UDP

| | |
|---|---|
| **Étudiant** | Mohamed Yakoubi |
| **Classe** | LSI 3 |
| **Matière** | Développement d'applications réparties |
| **Établissement** | Faculté des Sciences de Sfax |
| **Année universitaire** | 2026 – 2027 |

---

## Objectif du TP

Dans ce TP, je crée une application de **chat** entre un serveur et un ou plusieurs clients, avec des **sockets UDP** (mode non-connecté).

### Quelques mots importants

| Mot | Explication simple |
|---|---|
| **UDP (mode non-connecté)** | Il n'y a pas de connexion à établir : chaque message est envoyé seul, dans un paquet. |
| **Datagramme** | Un paquet UDP : des données (tableau d'octets) + l'adresse et le port du destinataire. |
| **`DatagramSocket`** | La socket UDP qui envoie (`send`) et reçoit (`receive`) des paquets. |
| **`DatagramPacket`** | L'objet Java qui représente un datagramme. |
| **`InetSocketAddress`** | Une adresse IP + un numéro de port. |

**Différence avec TCP :** en TCP, le serveur fait `accept()` et garde une socket par client. En UDP, le serveur a **une seule socket** et reçoit les paquets de tous les clients. C'est lui qui doit retenir les adresses des clients.

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
TP4-DAR2026
├── screenshots/
└── src/
    ├── clientPackage/
    │   ├── Client.java     (étape 2 : envoi seulement)
    │   └── Client2.java    (étape 3 : envoi + réception avec un thread)
    └── serverPackage/
        ├── Server.java     (étape 2 : affichage des messages)
        └── Server2.java    (étape 3 : liste des clients + diffusion)
```

---

## Étape 1 : Préparation de l'espace de travail

Le projet `TP4-DAR2026` est dans mon dépôt **`MohamedYakoubi_LSI3_DevAppReparties_TP`**.

---

## Étape 2 : Chat unidirectionnel et test avec un seul client

### 1. Côté serveur

Le serveur écoute sur le port `1234`, avec `InetSocketAddress` pour la liaison :

```java
DatagramSocket socket = new DatagramSocket(new InetSocketAddress(1234));
```

### 2. Côté client

Le client demande un nom d'utilisateur, puis envoie chaque message dans un datagramme, préfixé par le nom :

```java
String ch = "[" + username + "] : " + input;
byte buffer[] = ch.getBytes();
DatagramPacket donneesEmises = new DatagramPacket(buffer, buffer.length,
        InetAddress.getByName("localhost"), 1234);
socket.send(donneesEmises);
```

### 3. Traitement serveur (unidirectionnel)

Le serveur reçoit le paquet et affiche l'adresse (IP et port) de l'expéditeur, puis le message :

```java
socket.receive(paquet);
String donnees = new String(paquet.getData(), 0, paquet.getLength());
System.out.println("[" + paquet.getAddress().getHostAddress() + ":" + paquet.getPort() + "] " + donnees);
```

### 4. Exécution

![Le client envoie des messages](screenshots/step2%20client%20sends%20messages.png)

**Description :** Je tape mon nom `Mohamed`, puis trois messages.

![Le serveur reçoit les messages](screenshots/step2%20server%20receives%20messages.png)

**Interprétation du résultat :**

- Le serveur affiche chaque message avec l'adresse de l'expéditeur : `[127.0.0.1:63519] [Mohamed] : Bonjour serveur`.
- Le port `63519` n'est pas choisi par moi : c'est le système qui donne un port libre au client, car le client crée `new DatagramSocket()` sans numéro.
- Le chat est **unidirectionnel** : le client envoie, mais il ne reçoit rien.

---

## Étape 3 : Chat bidirectionnel et diffusion à plusieurs clients

### 1. Côté serveur : la liste des clients

`Server2` garde une liste des adresses (`SocketAddress`) des clients qui ont envoyé un message :

```java
static List<SocketAddress> clients = new ArrayList<>();
```

### 2. Côté client : envoyer et recevoir en même temps

`Client2` utilise **deux threads** :

- le **thread principal** lit le clavier et envoie les messages ;
- un **thread de réception** attend les messages du serveur avec `socket.receive(...)` et les affiche avec `[Reçu]`.

```java
Thread threadReception = new Thread(new Runnable() {
    public void run() {
        while (true) {
            socket.receive(packetReception);
            System.out.println("[Reçu] " + new String(packetReception.getData(), 0, packetReception.getLength()));
        }
    }
});
threadReception.start();
```

Sans ce thread, le client resterait bloqué sur `scanner.nextLine()` et ne verrait les messages des autres qu'après avoir tapé quelque chose.

### 3. Traitement serveur : la diffusion

```java
SocketAddress expediteur = paquet.getSocketAddress();
if (!clients.contains(expediteur)) {
    clients.add(expediteur);            // enregistrer le nouveau client
}
for (SocketAddress client : clients) {
    if (!client.equals(expediteur)) {   // tous sauf l'expéditeur
        socket.send(new DatagramPacket(paquet.getData(), paquet.getLength(), client));
    }
}
```

### 4. Test du service de chat

Je lance `Server2`, puis trois clients : `Ali`, `Sara` et `Mohamed`.

![Le serveur diffuse les messages](screenshots/step3%20server%20broadcast.png)

**Description :** Le serveur affiche `Nouveau client connecté` la première fois qu'un client envoie un message, puis chaque message avec l'adresse de son expéditeur. Chaque client a un port différent (`56561`, `64115`, `64116`).

![Client Ali](screenshots/step3%20client%20ali.png)

![Client Sara](screenshots/step3%20client%20sara.png)

![Client Mohamed](screenshots/step3%20client%20mohamed.png)

**Interprétation du résultat :**

- Ali reçoit les messages de Sara et de Mohamed (`[Reçu] ...`), mais **pas ses propres messages** : le serveur n'envoie pas le message à son expéditeur.
- Les messages arrivent **en temps réel**, pendant que le client attend une saisie, grâce au thread de réception.
- Sara n'a pas reçu le premier message d'Ali (`salut tout le monde`), et Mohamed n'a reçu que `bienvenue Mohamed`. C'est normal : en UDP il n'y a pas de connexion, donc le serveur ne connaît un client **qu'après son premier message**. Un client qui n'a encore rien envoyé ne reçoit rien.

---

## Comment lancer le projet

1. Ouvrir `TP4-DAR2026` dans IntelliJ IDEA.
2. **Étape 2 :** lancer `serverPackage.Server`, puis `clientPackage.Client`.
3. **Étape 3 :** lancer `serverPackage.Server2`, puis plusieurs `clientPackage.Client2`. Chaque client doit envoyer un premier message pour être enregistré.

> Les deux serveurs utilisent le port `1234` : il faut arrêter l'un avant de lancer l'autre.

---

## Conclusion

Dans ce TP, j'ai appris à :

- envoyer et recevoir des datagrammes avec `DatagramSocket` et `DatagramPacket` ;
- lier un serveur UDP à un port avec `InetSocketAddress` ;
- retrouver l'adresse de l'expéditeur d'un paquet (`getSocketAddress()`) ;
- utiliser un thread pour recevoir des messages pendant qu'on écrit ;
- diffuser un message à plusieurs clients en UDP.
