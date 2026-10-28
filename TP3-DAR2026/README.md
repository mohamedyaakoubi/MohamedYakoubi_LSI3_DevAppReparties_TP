# TP 3 : Serveur Multi-threads - sockets en mode connecté TCP

| | |
|---|---|
| **Étudiant** | Mohamed Yakoubi |
| **Classe** | LSI 3 |
| **Matière** | Développement d'applications réparties |
| **Établissement** | Faculté des Sciences de Sfax |
| **Année universitaire** | 2026 – 2027 |

---

## Objectif du TP

Jusqu'au TP2, mon serveur ne pouvait gérer **qu'un seul client** à la fois. Dans ce TP, je crée un **serveur multi-thread** : chaque client connecté a son propre **thread**, donc plusieurs clients peuvent travailler en même temps.

### Quelques mots importants

| Mot | Explication simple |
|---|---|
| **Thread** | Un « fil d'exécution ». Plusieurs threads tournent en parallèle dans le même programme. |
| **Serveur multi-thread** | Le thread principal accepte les clients ; pour chaque client, il lance un nouveau thread qui s'occupe de lui. |
| **Ressource critique** | Une donnée partagée entre plusieurs threads (ici le compteur d'opérations). |
| **`synchronized`** | Un seul thread à la fois peut exécuter la méthode : pas de conflit d'accès. |
| **Telnet** | Un client texte en ligne de commande qui se connecte à un serveur TCP. |

---

## Outils utilisés

| Outil | Version |
|---|---|
| Système | Windows 10 |
| Java (JDK) | OpenJDK 25 (Temurin) |
| IDE | IntelliJ IDEA (projet Maven) |
| Telnet | Client Telnet de Windows |

---

## Structure du projet

```text
TP3-DAR2026
├── pom.xml
├── screenshots/
└── src/main/java/
    ├── clientPackage/
    │   ├── Client_3_1.java          (activité 3-1)
    │   └── Client2.java             (activité 3-2 : client par objets)
    ├── commonPackage/
    │   ├── Operation.java
    │   └── Operation2.java          (objet Serializable envoyé au serveur)
    └── serverPackage/
        ├── ServerMultiThread.java   (activité 3-1)
        ├── ClientProcess.java       (activité 3-1 : thread d'un client)
        ├── ServerMultiThread2.java  (activité 3-2 : calculatrice + compteur)
        └── ClientProcess2.java      (activité 3-2 : thread d'un client)
```

---

## Activité 3-1 : Acceptation de plusieurs clients

### 1. Implémentation

**a) Serveur multi-thread**

Le serveur reste dans une boucle infinie. À chaque `accept()`, il crée un thread `ClientProcess` pour le nouveau client, puis il revient tout de suite attendre le client suivant.

```java
while (true) {
    Socket socket = serverSocket.accept();
    numero++;
    new ClientProcess(socket, numero).start();
}
```

**b) Afficher l'IP et le numéro d'ordre du client**

```java
System.out.println("Client n°" + numero + " connecté, adresse : " + socket.getRemoteSocketAddress());
```

**c) Envoyer au client son numéro d'ordre**

Dans `ClientProcess.run()`, le premier message envoyé au client est :

```java
writer.println("Bienvenue, vous êtes le client n°" + clientNumber);
```

Ensuite le thread renvoie chaque message du client (`Echo : ...`) jusqu'à `quit`.

### 2. Tests locaux

Je lance le serveur (port `1234`) :

![Lancement du serveur multi-thread](screenshots/3-1%20server%20run.png)

**Description :** Le serveur affiche `Le serveur attend la connexion d'un client…`.

**b) Des clients Java**

Je lance deux fois `Client_3_1` en même temps :

![Client Java n°1](screenshots/3-1%20java%20client%201.png)

![Client Java n°2](screenshots/3-1%20java%20client%202.png)

**Description :** Chaque client reçoit son propre numéro : `Bienvenue, vous êtes le client n°1` et `... n°2`. Les deux clients envoient un message et reçoivent leur écho **en même temps** : le premier client ne bloque pas le deuxième.

**a) Un client Telnet**

Dans une invite de commande, je tape `telnet localhost 1234` :

![Client Telnet](screenshots/3-1%20telnet%20client.png)

**Description :** Telnet reçoit `Bienvenue, vous êtes le client n°3`. Je tape `salut depuis telnet` et le serveur répond `Echo : salut depuis telnet`. Mon serveur Java accepte donc aussi un client qui n'est pas écrit en Java, car il parle simplement TCP avec des lignes de texte.

![Le serveur avec trois clients](screenshots/3-1%20server%20three%20clients.png)

**Interprétation du résultat :**

- Le serveur affiche un **numéro d'ordre** pour chaque client (n°1, n°2, n°3) et son **adresse** (IP + port).
- Les clients Java arrivent de `127.0.0.1` ; le client Telnet arrive de `[0:0:0:0:0:0:0:1]`, c'est `localhost` en **IPv6**.
- Les messages des trois clients sont traités en parallèle : chaque client a son thread.

### 3. Test en réseau local

Pour simuler une autre machine, je me connecte au serveur avec l'**adresse IP de ma carte Wi-Fi** (`192.168.100.9`) au lieu de `localhost`. La connexion passe alors par la carte réseau, comme pour un PC du même réseau :

![Client Telnet via l'adresse IP du réseau local](screenshots/3-1%20telnet%20client%20via%20LAN%20IP.png)

![Le serveur avec quatre clients](screenshots/3-1%20server%20four%20clients.png)

**Interprétation du résultat :** Le serveur affiche `Client n°4 connecté, adresse : /192.168.100.9:...`. Le client n°4 vient bien de l'adresse IP du réseau local, et il est servi en même temps que les trois autres. Sur un autre PC du même réseau, il suffit de taper la même commande avec l'IP du PC serveur.

Dans `Client_3_1`, l'adresse du serveur est donnée avec `InetAddress` et `InetSocketAddress` :

```java
socket.connect(new InetSocketAddress(InetAddress.getByName(IP_SERVEUR), PORT), 5000);
```

Il suffit de remplacer `IP_SERVEUR` (`"localhost"`) par l'adresse IP du PC serveur.

---

## Activité 3-2 : Amélioration de l'application calculatrice

### Étape 2 : Serveur multi-thread pour le service de calculatrice

Je crée `ServerMultiThread2` et `ClientProcess2` (je ne modifie pas les classes de l'activité 3-1). Le client `Client2` envoie un objet `Operation2` (`Serializable`), comme dans l'activité 2-2.

```java
// ClientProcess2 : le thread d'un client
outObj.writeObject("Bienvenue, vous êtes le client n°" + clientNumber);
while (true) {
    Operation2 op = (Operation2) inObj.readObject();
    double resultat = op.compute();
    outObj.writeObject(resultat);
    ...
}
```

`Client2` accepte l'adresse IP du serveur en argument (par défaut `localhost`), ce qui permet le test en réseau.

### Étape 3 : Gestion d'un accès concurrent

Le compteur global du nombre d'opérations est une **ressource critique** partagée par tous les threads `ClientProcess2`. Je l'incrémente dans une méthode `synchronized` :

```java
private static int nbOperations = 0;

public static synchronized int incrementerCompteur() {
    nbOperations++;
    return nbOperations;
}
```

Sans `synchronized`, deux threads pourraient lire la même valeur en même temps et écrire le même résultat : on perdrait une opération.

### Tests avec plusieurs clients (local et réseau)

Je lance le serveur, puis trois clients : deux avec `localhost` et un avec l'adresse IP `192.168.100.9` (`java ... clientPackage.Client2 192.168.100.9`). Les clients envoient leurs opérations à tour de rôle, sans attendre que les autres aient fini.

![Client 1](screenshots/3-2%20client%201.png)

![Client 2](screenshots/3-2%20client%202.png)

![Client 3 via l'adresse IP du réseau local](screenshots/3-2%20client%203%20via%20LAN%20IP.png)

**Description :** Chaque client reçoit son numéro (`n°1`, `n°2`, `n°3`) puis les résultats de **ses** opérations seulement. Le client 2 reçoit aussi un message d'erreur pour `7 / 0`. Le client 3 est connecté à `192.168.100.9:12345`.

![Le serveur et le compteur global](screenshots/3-2%20server%20counter.png)

**Interprétation du résultat :**

- Le serveur affiche, pour chaque opération, le numéro du client, le calcul et `total des opérations traitées`.
- Le compteur passe de **1 à 6** sans sauter de valeur et sans doublon, même si les opérations viennent de trois clients différents : il reste **cohérent**.
- La division par zéro du client 2 n'est **pas comptée** : le compteur n'est incrémenté que pour une opération réellement calculée.
- Le client 3 arrive de `/192.168.100.9` : le service marche aussi avec l'adresse IP du réseau local.

---

## Comment lancer le projet

1. Ouvrir `TP3-DAR2026` dans IntelliJ IDEA (projet Maven).
2. **Activité 3-1 :** lancer `serverPackage.ServerMultiThread`, puis plusieurs `clientPackage.Client_3_1` ou `telnet localhost 1234`.
3. **Activité 3-2 :** lancer `serverPackage.ServerMultiThread2`, puis plusieurs `clientPackage.Client2` (argument facultatif : l'adresse IP du serveur).

---

## Conclusion

Dans ce TP, j'ai appris à :

- créer un serveur multi-thread (un thread par client) ;
- afficher l'adresse et le numéro d'ordre de chaque client avec `getRemoteSocketAddress()` ;
- tester un serveur TCP avec Telnet et avec une adresse IP du réseau local ;
- protéger une ressource partagée avec `synchronized`.
