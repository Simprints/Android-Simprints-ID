# How to use the session event repository

The session event repository is an in-memory cache backed by an encrypted Room DB  that stores events in a session.
To ensure correct event sequence and avoid issues (like split sessions) all functions of the repository should be called in SessionCoroutineScope
