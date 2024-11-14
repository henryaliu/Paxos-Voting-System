How my Paxos works:
* Random chance of 1 or 2 proposers
* Proposals are assigned to a random member, neutrally
* Members 1, 2, and 3 due to their 'dominance' will more likely nominate themselves if picked to send a proposal
* Member 2 is more likely to nominate itself than the others, Members 1 and 2 may nominate someone exceptional rarely
* Other members nominate a random member without bias

* Response times of each Member are implemented and simulated in Acceptor class when responding to Proposer class