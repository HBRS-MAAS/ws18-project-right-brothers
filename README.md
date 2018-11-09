[![Build Status](https://travis-ci.org/HBRS-MAAS/ws18-project-right-brothers.svg?branch=master)](https://travis-ci.org/HBRS-MAAS/ws18-project-right-brothers)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f65d632f35334321a8ee3a5feaf6a26c)](https://www.codacy.com/app/DRealArun/ws18-project-right-brothers?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=HBRS-MAAS/ws18-project-right-brothers&amp;utm_campaign=Badge_Grade)
[![Coverage Status](https://coveralls.io/repos/github/HBRS-MAAS/ws18-project-right-brothers/badge.svg?branch=master)](https://coveralls.io/github/HBRS-MAAS/ws18-project-right-brothers?branch=master)

# MAAS Project - right-brothers

Add a brief description of your project. Make sure to keep this README updated, particularly on how to run your project from the **command line**.

## Team Members
*   Dharmin Bakaraniya - [@DharminB](https://github.com/DharminB)
*   Arun Rajendra Prabhu - [@DRealArun](https://github.com/DRealArun)
*   Md Zahiduzzaman - [@ZahidSE](https://github.com/ZahidSE)

## Dependencies
* JADE v.4.5.0
* ...

## How to run
Just install gradle and run:

    gradle run

It will automatically get the dependencies and start JADE with the configured agents.

### Running from different machines

#### Main Container

    gradle run --args="server"

#### Peripheral Container

    gradle run --args="-host <host ip address>"

In case you want to clean you workspace run

    gradle clean

## Unit test

    gradle test

## Eclipse
To use this project with eclipse run

    gradle eclipse

This command will create the necessary eclipse files.
Afterwards you can import the project folder.

## Team convention

### Code
- No commented out code should be checked in as it makes the code less readable. This is often done for print statements used during debugging. If other members find a commented out code he/she will remove it without confirming and without complaining. However, if there is some reason to comment out code it should explain the reason at first line.

    //TODO - I found this nice code from stackoverflow but not sure how to use it
    /**
     *
     */
    
- Line comments should be avoided as much as possible and code should be self explanatory. Doc comment is encouraged but not mandatory.

### Git
- Multiple members working on same feature should use same branch. Having multiple branches makes merging more difficult.