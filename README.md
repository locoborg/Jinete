# Jinete: Java-Based 3D Navigator of the Internet (1998)

![Award](https://img.shields.io/badge/ACM_Contest-3rd_Place_(1998)-gold)
![Java Version](https://img.shields.io/badge/Java-1.1%20/%201.2-red)
![Platform](https://img.shields.io/badge/Platform-Windows%2095%2F98%20%7C%20Solaris-lightgrey)

**Jinete** (Spanish for "Rider") is a 3D network visualization tool developed in 1998 by engineering students at the University of Oklahoma. It won **3rd Place** in the national Java Programming Contest sponsored by the **ACM (Association for Computing Machinery)**.

Before the modern security landscape of firewalls and DDoS protections, Jinete allowed users to "walk" through the physical structure of the internet using ICMP discovery and a unique spatial mapping of the IPv4 address space.

---

## 🌐 The Concept

In 1998, the internet was often referred to as the "Information Superhighway." Jinete took this literally, transforming the abstract concept of IP addresses into a navigable 3D galaxy.

### Spatial Mapping Logic
The core engine translated an IPv4 address `(A.B.C.D)` into a 3D coordinate system:
* **Cluster Position ($X, Y, Z$):** Determined by the first three octets of the IP address.
* **Local Position ($d$):** The fourth octet determined the specific placement of the device within that spatial cluster.

This allowed users to start from their own public IP and literally see their neighbors on the local subnet before "flying" to distant networks.

## 🛠 Features

* **Active Discovery:** Utilized `ping` (ICMP) to detect live devices in real-time.
* **3D Traversal:** A first-person perspective navigation engine built during the early days of Java 3D and high-performance JVMs.
* **Network Topology Visualization:** Provided a visual sense of "distance" between networks that a standard terminal could not convey.

## 🏆 Historical Significance

The project was recognized by the **ACM** for its innovative use of Java's then-emerging networking and graphical capabilities. It represents a snapshot of an era when the internet was an open frontier, and global device discovery was possible from a single desktop application.

## 📜 Original Tech Stack (circa 1998)

* **Language:** Java (JDK 1.1.x / 1.2 Beta)
* **Graphics:** ThreeD.java and VRML.java
* **Networking:** Standard `java.net` and native ICMP calls for pinging.
* **Development Environment:** simple Notepad/JDK.

---

## 📂 Project Status
**Note:** This repository serves as a historical archive. Due to the evolution of internet security and the deprecation of early Java graphics libraries, this software is provided "as-is" for educational and nostalgic purposes.

> "We didn't just want to browse the web; we wanted to ride through it." 
> — *The Jinete Team, 1998*
