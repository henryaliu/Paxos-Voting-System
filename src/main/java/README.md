# How my Paxos-based election system works:
1. Proposer: sends "Prepare"
2. Acceptor: responds "Promise" if received id > current id, else "Reject"
3. Proposer: Check for majority (half + 1) Promise and then send "Accept", else end election
4. Acceptor: responds "Accepted" if received id > current id
5. Proposer: checks for majority (half + 1) "Accepted", and then informs each Learner representing each Member
Note: For sake of liveness, a timeout limit of 10 seconds is used. If no response after 10 seconds, we assume no response from a node.

# HOW TO RUN
1. Ensure Java 1.2 or later is used (because of getFirst() feature for hashmaps)
2. Ensure Acceptor, Learner, Member, MemberManagement, and Proposer are all in the same folder (java folder)
3. Compile the code in the java folder in the terminal using: "javac *.java"
4. Run the program using "java MemberManagement". When you run it, a random chance of 1 or 2 proposals is chosen.
5. The Paxos election process will automatically take place, you may sit back for a few seconds and relax 
6. You can view the status and results which are printed in the terminal.
7. The election usually finishes within 20 seconds, depending on the response times of each Member.
8. Ctrl + C to end the program. Repeat steps 4 and 8 however many times you wish.
9. Note: Each member has their own profile (3rd Criteria) by default, response times are random but within a distinguishable range 

# 2 SIMULTANEOUS PROPOSALS WORKS (1st Criteria)
* There is a 30% chance of 2 proposals when you run the program. 
* Just keep running the program until the terminal indicates 2 proposals are sent for proof.
* It will say: "*** 2 proposal(s) for the next President have been submitted! ***"

# HOW TO RUN ELECTION WHERE ALL MEMBERS HAVE IMMEDIATE RESPONSES (2nd criteria)
1. Follow steps 1-2 from "HOW TO RUN" section above
2. In MemberManagement class ("MemberManagement.java"), locate line 126 (MemberManagement m = new MemberManagement())
3. Add 'true' into the argument of the constructor. (E.g. MemberManagement m = new MemberManagement(true))
4. This changes the profiles of each Member to have instant responsiveness (Tier 1 responsiveness)
5. Run steps 3-8 from "HOW TO RUN" section above. The results should now be returned almost instantly.

# Other details
* The election is automatically concluded when a majority consensus (half + 1) is received for a Proposal, even if a small 
minority of Members haven't responded. This applies for instant responses too (assume the council computers are very fast)
Reason: Previous proposals have no chance of catching up once 1 proposal has been committed to be "Accepted" by majority of Acceptors
* Random chance of 1 or 2 proposers
* Proposals are assigned to a random member, neutrally. Proposal IDs are unique and increasing.
* Members 1, 2, and 3 due to their 'dominance' will more likely nominate themselves if picked to send a proposal,
but the randomness of which member gets to submit a proposal is still fully random without bias.
* Member 2 is more likely to nominate itself than the others, Members 1 and 2 may nominate someone exceptional rarely
* Other members nominate a random member without bias if selected to submit a proposal.
* Response times of each Member are implemented and simulated in Acceptor class when responding to Proposer class

Unit/Integration tests are also provided for the three criteria