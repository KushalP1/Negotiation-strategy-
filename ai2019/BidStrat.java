package ai2019.group30;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.NoModel;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;

/**
 * This is an abstract class used to implement a TimeDependentAgent Strategy
 * adapted from [1] [1] S. Shaheen Fatima Michael Wooldridge Nicholas R.
 * Jennings Optimal Negotiation Strategies for Agents with Incomplete
 * Information http://eprints.ecs.soton.ac.uk/6151/1/atal01.pdf
 * 
 * The default strategy was extended to enable the usage of opponent models.
 */
public class BidStrat extends OfferingStrategy {

	/**
	 * k in [0, 1]. For k = 0 the agent starts with a bid of maximum utility
	 */
	static int flag=0;
	static double fl;
	private double k;
	/** Maximum target utility */
	private double Pmax;
	/** Minimum target utility */
	private double Pmin;
	/** Concession factor */
	private double e;
	/** Outcome space */
	private SortedOutcomeSpace outcomespace;

	/**
	 * Method which initializes the agent by setting all parameters. The
	 * parameter "e" is the only parameter which is required.
	 */
	@Override
	public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms,
			Map<String, Double> parameters) throws Exception {
		super.init(negoSession, parameters);
		if (parameters.get("e") != null) {
			this.negotiationSession = negoSession;

			outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
			negotiationSession.setOutcomeSpace(outcomespace);

			this.e = parameters.get("e");

			if (parameters.get("k") != null)
				this.k = parameters.get("k");
			else
				this.k = 0;

			if (parameters.get("min") != null)
				this.Pmin = parameters.get("min");
			else {
				this.Pmin = negoSession.getMinBidinDomain().getMyUndiscountedUtil();
			}

			if (parameters.get("max") != null) {
				this.Pmax = parameters.get("max");
			} else {
				BidDetails maxBid = negoSession.getMaxBidinDomain();
				this.Pmax = maxBid.getMyUndiscountedUtil();
			}

			this.opponentModel = model;
			
			this.omStrategy = oms;
		} else {
			throw new Exception("Constant \"e\" for the concession speed was not set.");
		}
	}

	@Override
	public BidDetails determineOpeningBid() {
		return determineNextBid();
	}

	/**
	 * Simple offering strategy which retrieves the target utility and looks for
	 * the nearest bid if no opponent model is specified. If an opponent model
	 * is specified, then the agent return a bid according to the opponent model
	 * strategy.
	 */
	@Override
	public BidDetails determineNextBid() {
		double time = negotiationSession.getTime();
		double utilityGoal;
		utilityGoal = nextBidUtil();

		// if there is no opponent model available
		if (opponentModel instanceof NoModel) {
			nextBid = negotiationSession.getOutcomeSpace().getBidNearUtility(utilityGoal);
		} else {
			nextBid = omStrategy.getBid(outcomespace, utilityGoal);
		}
		return nextBid;
	}



	/**
	 * Place the next bid with utility greater than the previous bid if the flag is 1 or with the utility less than the previous bid if the flag is 2.
	 * @return double
	 */
	public double nextBidUtil() {
		Bid lastOwnBid = negotiationSession.getOwnBidHistory().getLastBid();
		double myLastUtil = 0;
		if (lastOwnBid != null) {
			myLastUtil = negotiationSession.getUtilitySpace().getUtility(lastOwnBid);
		}

		if(flag == 0){
			flag = 1;
			return (Pmax+Pmin)/2.0;
		} 
		if(flag == 1){
		double bidUtil = (myLastUtil*0.1 + myLastUtil);
			if(bidUtil>=(Pmax-0.5)){
			
				flag=2; 
			}
		return bidUtil;
		}
		else{
		double bidUtil = (myLastUtil - myLastUtil*0.1);
			if( bidUtil <= (Pmin + 0.5) ){
			
				flag=1; 
			}
		return bidUtil;
		}
	}

	public NegotiationSession getNegotiationSession() {
		return negotiationSession;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("e", 1.0, "Concession rate"));
		set.add(new BOAparameter("k", 0.0, "Offset"));
		set.add(new BOAparameter("min", 0.0, "Minimum utility"));
		set.add(new BOAparameter("max", 0.99, "Maximum utility"));

		return set;
	}

	@Override
	public String getName() {
		return "TimeDependent Offering example";
	}
}
