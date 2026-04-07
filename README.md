# Jinete: Java-Based 3D Navigator of the Internet (1999)

![Award](https://img.shields.io/badge/ACM_Contest-3rd_Place_(1999)-gold)
![Java Version](https://img.shields.io/badge/Java-1.2_(Java_2)-red)
![Platform](.../Tijuana_Context) 

**Jinete** (Spanish for "Rider") is a legacy 3D network visualization engine developed in 1999. Created by Jose Enrique Segura Luquin and an engineering partner at the University of Oklahoma, the project earned **3rd Place** in the prestigious Java Programming Contest sponsored by the **ACM (Association for Computing Machinery)**.

Built during the "Wild West" era of the internet, Jinete reimagined the IPv4 address space as a physical, navigable landscape.

---

## 🌐 The Concept: Mapping the IP Galaxy

In 1999, the internet was a more transparent frontier. Jinete functioned by translating the logical structure of IPv4 addresses directly into Euclidean 3D coordinates:

* **Cluster Coordinates ($X, Y, Z$):** Derived from the first three octets of an IP address, defining a network's "sector" in space.
* **Local Position:** The 4th octet determined the specific placement of a device within that cluster.

### 📡 Discovery & Navigation
Starting from the user's own public IP, the application used **ICMP (Ping)** to discover active devices. Users could literally "ride" from their local network out into the wider internet, visualizing the proximity of devices and the vastness of the global network in real-time—a feat made possible by the lack of widespread firewalls and DoS concerns at the time.

## 🏆 Achievements
* **Award:** 3rd Place, ACM Java Programming Contest (1999).
* **Innovation:** Early implementation of spatial network topology using Java’s emerging networking libraries.

## 🛠 Historical Tech Stack
* **Language:** Java 2 (JDK 1.2).
* **Networking:** `java.net` utilizing ICMP for device discovery.
* **Environment:** Developed during the transition to the modern Java platform.

---

## 📂 Repository Note
This repository is a historical archive of the 1999 award-winning project. It stands as a testament to early 3D web experimentation and the evolution of network security and visualization.

> "A journey through the internet should be more than a series of links; it should be a ride through a digital universe."
