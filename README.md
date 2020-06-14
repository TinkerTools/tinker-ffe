# Tinker-FFE: Molecular Engineering GUI for Tinker

<H2><B>Introduction</B></H2>

The Tinker-FFE ("Force Field Explorer") software is a Java-based GUI for molecular design and engineering, originally written by Prof. Michael J. Schnieders, now in the Department of Biomedical Engineering at the University of Iowa. In addition to a variety of general molecular modeling and visualization capabilities, Tinker-FFE enables launch and control of Tinker calculations from an easy-to-use graphical interface. Molecular and biomolecular structures can be downloaded from the PubChem, NCBI and PDB databases. The Tinker-FFE package runs on Linux, Macintosh and Windows systems.

<H2><B>Installation Kits</B></H2>

Pre-built installation kits for the Tinker-FFE package can be found at https://dasher.wustl.edu/tinker/, and are available for Linux, Macintosh and Windows computers. Tinker-FFE contains an embedded version of the full Tinker software package, modified to provide for socket communication between Tinker and the Tinker-FFE GUI via Java sockets. Unless you wish to develop Tinker-FFE, we recommend use of these kits since the build process is rather involved.

<H2><B>Build Process</B></H2>

The Tinker-FFE project builds installer kits using the Install4j multi-platform Java installer builder. Other requirements include a Java 8 JRE to embed with the installer, Ant, and C/C++/Fortran compilers (the GNU compiler suite is recommended). The graphics engine for FFE uses Java3D and the FFE console requires groovy; both packages are provided with this development repository. To begin the build procedure, see the 0README file in the /build directory of the repository.

![alt text](https://github.com/TinkerTools/Tinker-FFE/blob/release/other/install4j/icon-large.png?raw=true)
