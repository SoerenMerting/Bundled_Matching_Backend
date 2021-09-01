# Bundled_Matching_Backend
Source code for the algorithms of TUM matching system for allocating tutorial seats

This repository contains the algorithms and basic datastuctures for an add on to the TUM Matching System for seminars and lab courses. As the original system is not open source, the related code cannot be published here. Hence, the provided code on its own is not ready to run and it still needs to be implemented a small software around (basic database structure, user management, home page etc.). Some additional information you might find in doc.txt.

Content:
* algorithms: contains algorithms for running BPS, BRSD, as well as bundle generation and bundle scoring (and automatic ranking)
* datastructures & models: basic datastructures for assignments, instances, preference lists, students, courses, ... to help understanding the algorithms
* maxcu_bps: contains the MAXCU decomposition algorithm for decomposing a fractional solution into a lottery of integral solutions, adjusted to be used with BPS. For running these optimizations gurobi702 is needed.
* maxcu-interface: helper for transferring data between BPS (python) and MAXCU (java). It is recommended to use py4j0.10.2.1
