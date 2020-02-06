package bilateralexamples;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bilateralexamples.boacomponents.Acceptance;
import bilateralexamples.boacomponents.BidStrat;
import bilateralexamples.boacomponents.OpponentMod;
import bilateralexamples.boacomponents.OpponentModStrat;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.BoaParty;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.Bid;
import genius.core.utility.AbstractUtilitySpace;

/**
 * Negotiation agent by group 30 using the BOA framework.
 * @author Group 30
 */
@SuppressWarnings("serial")
public class Group30_Party extends BoaParty
{

    private int learnValueAddition;
    /**
     * Initializes the negotiation agent using the separate components for the strategy.
     * @param info
     */
    @Override
    public void init(NegotiationInfo info)
    {
        // The choice for each component is made here
        AcceptanceStrategy 	ac  = new Acceptance();         // acceptance strategy module
        OfferingStrategy 	os  = new BidStrat();           // bidding strategy module
        OpponentModel 		om  = new OpponentMod();        // opponent model module
        OMStrategy			oms = new OpponentModStrat();   // opponent module strategy module

		// All component parameters can be set below.
		Map<String, Double> noparams = Collections.emptyMap();
		Map<String, Double> osParams = new HashMap<String, Double>();

        // Set the concession parameter "e" for the offering strategy to yield Boulware-like behavior
		osParams.put("e", 0.2);

        // Initialize all the components of this party to the choices defined above
        configure(ac, noparams, os,	osParams, om, noparams, oms, noparams);
        super.init(info);
    }


    /**
     * PREFERENCE UNCERTAINTY:
     *
     * Specific functionality, such as the estimate of the utility space in the
     * face of preference uncertainty, can be specified by overriding the
     * default behavior.
     *
     * This example estimator sets all weights and all evaluator values randomly.
     */
    @Override
    public AbstractUtilitySpace estimateUtilitySpace()
    {
        AdditiveUtilitySpaceFactory additiveUtilitySpaceFactory =
                new AdditiveUtilitySpaceFactory(getDomain());

        BidRanking bidRanking = userModel.getBidRanking();
        List<Bid> bids = bidRanking.getBidOrder();               // bid list sorted low U to high U
        List<IssueDiscrete> issues = additiveUtilitySpaceFactory.getIssues();   // issue list in the domain
        int sizeBidRankingList = bids.size();

        Bid prevBid = bids.get(sizeBidRankingList-1);
        HashMap<Integer, Integer> totalIssueChanges = new HashMap<Integer, Integer>();

        /* UPDATING THE ISSUE WEIGHTS: */
        // initializing each issue with 0 change.
        for (Issue i : issues) {
            totalIssueChanges.put(i.getNumber(), 0);
        }

        // adding a one to each issue each time the value remained the same.
        for (int i = sizeBidRankingList-2; i >= 0; i--) {
            Bid currBid = bids.get(i);
            updateDifference(issues, totalIssueChanges, 1, prevBid, currBid);
            prevBid = currBid;
        }

        // normalization + final update of each issue weight:
        int sumWeights = 0;
        for (IssueDiscrete i : issues) {
            sumWeights += totalIssueChanges.get(i.getNumber());
        }

        for (IssueDiscrete i : issues) {
            int oldWeight = totalIssueChanges.get(i.getNumber());
            double newWeight =  (double) oldWeight / sumWeights;
            additiveUtilitySpaceFactory.setWeight(i, newWeight);
        }

        /*UPDATING VALUE WEIGHTS: */
        //In the top 10 % of bids, counting the occurences of values.

        double topFraction = 0.1;
        int sizeTopBids = (int) Math.ceil(topFraction*sizeBidRankingList);
        int sizeBottomBids = sizeBidRankingList - sizeTopBids;

        for (IssueDiscrete i: issues) {
            HashMap<ValueDiscrete, Integer> totalValues = new HashMap<ValueDiscrete, Integer>();
            for (ValueDiscrete val: i.getValues()) {
                totalValues.put(val, 0);
            }

            for (ValueDiscrete val: i.getValues()) {
                for (int j = sizeBottomBids; j < sizeBidRankingList-1; j++) {
                    Bid currBid = bids.get(j);
                    ValueDiscrete value = (ValueDiscrete) currBid.getValue(i.getNumber());
                    int oldCount = totalValues.get(value);
                    totalValues.replace(value, oldCount + 1);
                }
            }

            int maxValueCount = 0;
            for (ValueDiscrete val : totalValues.keySet()) {
                if (totalValues.get(val) > maxValueCount){
                    maxValueCount = totalValues.get(val);
                }
            }

            for (ValueDiscrete val : totalValues.keySet()) {
                int countValue = totalValues.get(val);
                double valWeight = (double) countValue / maxValueCount;
                additiveUtilitySpaceFactory.setUtility(i, val, valWeight);
            }
        }
        // Normalize the weights, since we picked them randomly in [0, 1]
        additiveUtilitySpaceFactory.normalizeWeights();

        // The factory is done with setting all parameters, now return the estimated utility space
        return additiveUtilitySpaceFactory.getUtilitySpace();
    }

    /**
     * Updates the count of each issue, when two subsequent bids have the same issues.
     *
     */
    private void updateDifference(List<IssueDiscrete> issues,
                                                       HashMap<Integer, Integer> totalIssueChanges,
                                                       int increment,
                                                       Bid first,
                                                       Bid second) {

        HashMap<Integer, Integer> diff = new HashMap<Integer, Integer>();

        try {
            for (IssueDiscrete i : issues) {
                Value value1 = first.getValue(i.getNumber());
                Value value2 = second.getValue(i.getNumber());
                int oldValue = totalIssueChanges.get(i.getNumber());
                if (value1.equals(value2)) {
                    totalIssueChanges.replace(i.getNumber(), oldValue + increment);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getDescription()
    {
        return "Group 30 Negotiation Agent";
    }
}
