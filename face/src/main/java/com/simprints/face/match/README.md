# How matching works

SimprintsID send the face modality a FaceMatchRequest with a list of FaceSamples (called probes) and a query to load all the candidates from the database. Matching needs to compare the probes templates with all the candidates face templates and return a list of X candidates sorted descending by their higher comparison scores.

- SimprintsID gives a list of FaceSamples because the Capture flow can return more than one capture (depending on the configuration). Each probe has its own template.
- Each candidate have a guid and a list of FaceSamples.

What this means is that the number of comparisons will be:

```
p * c * cf
```

p - number of probes
c - number of candidates
cf - number of faces per candidate

Example: if the configuration say that each capture has 2 faces, and the database already has 3 people, 12 comparisons will be made (2 * 3 * 2).

## Sorting the comparison scores

For response, SimprintsID expects a MatchResult comprising of a guid and confidence score. This means that no matter how many faces the candidate has, only the highest comparison score will be return.

What is being done is during a comparison of probes against a candidate, only the highest score of all is kept and added to the MatchResult.

Example:
```
probe1 x candidate1.face1 = 0.7
probe1 x candidate1.face2 = 0.6
probe2 x candidate1.face1 = 0.8
probe2 x candidate1.face2 = 0.1
```

The value 0.8 will be the one assigned to candidate1 MatchResult.

# Concurrency

Because the process of matching can be expensive - it needs to match `n` probes against `f` candidate faces - we tried to run it in parallel. That way, we can run multiple matchings at the same time. Also, since the list will be ordered later, we don't need to care about the order that the results are returned as well.

There is a bit of overhead when creating a new flow so there is a [test](../../../../../../../../core/src/test/java/com/simprints/core/tools/extentions/FlowKtTest.kt) that proves that even if the operation takes 1 millisecond, running in parallel it is times faster than sequential (not counting the overhead).
