package com.simprints.fingerprintmatcher.sourceafis.matching;

import com.simprints.fingerprintmatcher.sourceafis.matching.minutia.MinutiaMatcher;
import com.simprints.fingerprintmatcher.sourceafis.meta.Nested;
import com.simprints.fingerprintmatcher.sourceafis.meta.ParameterSet;
import com.simprints.fingerprintmatcher.sourceafis.templates.Template;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
public class ParallelMatcher
{
    @Nested
    public MinutiaMatcher minutiaMatcher = new MinutiaMatcher();

    public class PreparedProbe
    {
        public ProbeIndex ProbeIndex = new ProbeIndex();
    }

    Queue<MinutiaMatcher> matchers = new LinkedList<MinutiaMatcher>();
  /*
   * Review the code, it is creaating new matcher everytime
   */
    MinutiaMatcher dequeueMatcher()
    {
        MinutiaMatcher matcher = null;
       // lock (Matchers)
        synchronized(matchers){
            if (matchers.size() > 0)
             matcher = matchers.poll();
        }
       if (matcher == null) {
    	   matcher = ParameterSet.clonePrototype(minutiaMatcher);
       }
        return matcher;
    }

    MinutiaMatcher DequeueMatcher(PreparedProbe probe)
    {
        MinutiaMatcher matcher = dequeueMatcher();
        matcher.SelectProbe(probe.ProbeIndex);
        return matcher;
    }

    void enqueueMatcher(MinutiaMatcher matcher)
    {
    	synchronized(matchers){
             matchers.offer(matcher);
    	}
    }

    public PreparedProbe prepare(Template probe)
    {
        PreparedProbe prepared = new PreparedProbe();
        MinutiaMatcher matcher = dequeueMatcher();
        try
        {
            matcher.BuildIndex(probe, prepared.ProbeIndex);
        }
        finally
        {
            enqueueMatcher(matcher);
        }
        return prepared;
    }

    public float[] Match(PreparedProbe probe, List<Template> candidates)
    {
        float[] scores = new float[candidates.size()];
        // No Parallel , use Sequencial for now
        for(int x=0;x<scores.length;x++){
          MinutiaMatcher matcher= DequeueMatcher(probe);
          scores[x]=matcher.Match(candidates.get(x));
          enqueueMatcher(matcher);
        }
        
        /*
        Parallel.For(0, candidates.Count,
            () => DequeueMatcher(probe),
            (i, state, matcher) => { scores[i] = matcher.Match(candidates[i]); 
            return matcher;
            },
            (matcher) => { EnqueueMatcher(matcher); });
         */  
        return scores;
    }
}

