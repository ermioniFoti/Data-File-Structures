# Disk File Organization & Search Analysis

A low-level data management system developed in Java to simulate and analyze different disk storage organizations and search algorithms. This project was developed as part of the **Data Structures and Algorithms** course at the **Technical University of Crete** (Spring 2022).

## 📌 Project Overview
The primary objective of this application is to manage data records stored on a disk using **Page-Based I/O**. Unlike standard in-memory applications, this system reads and writes data in fixed-size blocks (Disk Pages), simulating the behavior of database management systems (DBMS).

The project implements and compares three distinct methods of organizing and retrieving data to measure performance in terms of **Disk Accesses** and **Execution Time**.

## 🚀 Implemented Storage Methods

The application manages `DataClass` records (consisting of a unique 4-byte Integer Key and a String payload) using the following architectures:

### 1. Method A: Serial Organization (Brute Force)
* **Structure:** Records are stored sequentially in the main data file in random order.
* **Search Algorithm:** Linear Search.
* **Process:** The system reads every page from the beginning until the target key is found.
* **Complexity:** High disk I/O overhead ($O(N)$).

### 2. Method B: Unsorted Indexing
* **Structure:**
    * **Main File:** Same as Method A (Sequential, random order).
    * **Index File:** A separate file containing `<Key, PageID>` pairs. This file is smaller and denser but remains unsorted.
* **Search Algorithm:** Linear Search on the Index $\rightarrow$ Direct Access on Main File.
* **Process:** The system scans the index file to find the Page ID, then performs a single read on the main file.
* **Advantage:** Reduces I/O volume compared to Method A because index pages hold more entries than data pages.

### 3. Method C: Sorted Indexing (Binary Search)
* **Structure:**
    * **Main File:** Same as Method A.
    * **Index File:** A separate file containing `<Key, PageID>` pairs, **sorted by Key**.
* **Search Algorithm:** Binary Search on the Index $\rightarrow$ Direct Access on Main File.
* **Process:** Uses the sorted nature of the index to perform a binary search (loading specific index pages), locating the target in logarithmic time.
* **Advantage:** drastically reduces disk accesses ($O(\log N)$).


## 🛠️ Technical Specifications
* **Language:** Java.
* **I/O Mechanism:** `java.nio.ByteBuffer` and `RandomAccessFile` for direct byte manipulation.
* **Page Size:** Fixed at **256 Bytes**.
* **Data Serialization:** Custom methods to convert Objects $\leftrightarrow$ Byte Arrays.
* **Simulation Scale:** Tested with dataset sizes ranging from **50** to **200,000** records.

## 📊 Performance Analysis
The project includes a comprehensive benchmarking suite that generates random datasets and performs 1,000 search operations per test case to calculate:
1.  **Average Disk Accesses:** The number of pages loaded from the disk into RAM.
2.  **Average Search Time:** Measured in nanoseconds.

### Experimental Variables
* **Record Size A:** 59 Bytes (4B Key + 55B Data).
* **Record Size B:** 31 Bytes (4B Key + 27B Data).


## 📂 Project Structure
* **`DataClass`:** Represents the core entity with methods for serialization.
* **`DataPage`:** Manages the packing of records into fixed-size byte arrays.
* **`FileManager`:** Handles low-level `RandomAccessFile` operations (Read/Write pages).
* **`Main`:** The driver class that executes the experiment, generates random keys/data, and outputs performance metrics.

---
*Developed for the School of Electrical and Computer Engineering, Technical University of Crete.*
