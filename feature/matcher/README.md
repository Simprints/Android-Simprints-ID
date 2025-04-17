# How matching works

Calling feature module provides `MatchParams` in navigation arguments with a list of either `FaceSample` or `FingerprintSample` and a query
to load the candidates from the database.

Matching needs to compare the probes templates with all the candidates templates and return a list of X candidates sorted descending by
their higher comparison scores.

- Sample list is provided because the Capture flow can return more than one capture (depending on the configuration). Each probe has its own
  template.
- Each candidate have a guid and a list of samples.

What this means is that the number of comparisons will be:

```
p * c * cs
```

p** - number of probes, **c** - number of candidates, **cs** - number of samples per candidate

Example: if the configuration say that each capture has 2 samples, and the database already has 3
people, 12 comparisons will be made (2 * 3 * 2).

## Sorting the comparison scores

For response, calling module expects a MatchResult with a list of a guid and confidence score pairs. This means that no matter how many
samples the candidate has, only the highest comparison score will be return.

What is being done is during a comparison of probes against a candidate, only the highest score of all is kept and added to the MatchResult.

Example:

```
probe1 x candidate1.sample1 = 0.7
probe1 x candidate1.sample2 = 0.6
probe2 x candidate1.sample1 = 0.8
probe2 x candidate1.sample2 = 0.1
```

The value 0.8 will be the one assigned to candidate1 MatchResult.

The reason behind returning `max` instead of `mean` is because there is a probability that the
person can take 2 photos but one of them is from the wrong person. Or that the other photo has no
one in there - the user moves the phone too fast. So thinking about that, both cases would drag
the `mean` down as (1 + 0) / 2 = 0.5.

# Concurrency

Because the process of matching can be expensive - it needs to match `n` probes against `f` candidate faces - we tried to run it in batches
in parallel.
That way, we can run multiple matching at the same time. Also, to reduce memory pressure the results are stored in a limited sorted tree.
