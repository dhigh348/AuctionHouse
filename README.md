# Project: NAME OF PROJECT
## Student(s):  Name(s)

## Introduction
Generally describe the project/problem you are solving.

## Contributions
If this is a group project then detail which group member worked on what aspect(s) of the project.

## Usage
Give details about how to use the program. (Imagine making it easy for someone that you don't know to run and use your project.)

## Project Assumptions
This section is where you put any clarifications about your project & how it works concerning any vagueness in the specification documents.

## Versions
Where are the .jar files?
### V1
explain about how version 1 works
### V2
explain about how version 2 works etc...

## Docs
What folder is your documentation (diagram and class diagram) in?

## Status
### Implemented Features
State things that work.

### Known Issues
If there are things that don't work put them here. It will save the graders time and keep them in a good mood.

## Testing and Debugging
If you have tests, then explain how they work and how to use them.


## Daniel's Todo's
How are we synchronizing bank accounts? Each time balances are update, do we send their new account object to Agent/AuctionHouse?
How are we handling errors such as too few args in a money transfer? Send an error message back?
Why does the bank proxy have a run method? It isn't a thread in roman's design.
Add an UNBLOCK_FUNDS message type
When I create an account, I am assuming that agents and auction houses will send an
    AgentInfo or AuctionInfo object to bank.
Is it okay to send THANKS back as the default in my cases?
Once funds are blocked in an agent account, how do I send the agent's new account back to him?