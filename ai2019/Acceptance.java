package ai2019.group30;

import java.util.Map;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;

/**
 * Class containing the implementation of the acceptance strategy of group 30.
 */
public class Acceptance extends AcceptanceStrategy {

    /**
     * Minimum utility we desire during the first phase of the negotiation.
     */
    private double alpha = 0.85;

    /**
     * Empty constructor for the BOA framework.
     */
    public Acceptance() {
    }

    /**
     * Constructor for the acceptance strategy component.
     * @param negoSession The current negotiation session.
     * @param strat The bidding strategy used by the BOA party.
     */
    public Acceptance(NegotiationSession negoSession, OfferingStrategy strat) {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;
    }

    @Override
    /**
     * Initializes this acceptance component.
     */
    public void init(NegotiationSession negoSession, OfferingStrategy strat,
                     OpponentModel opponentModel, Map<String, Double> parameters)
            throws Exception {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;
    }

    /**
     * Determines whether to accept the opponents bid or not.
     * Accepts when the opponents bid has a higher utility for us than our next bid.
     * Accepts if there is almost no time left.
     * Accepts if the utility for us is higher than we desire at that time.
     * Otherwise reject.
     * @return The accept or reject action.
     */
    @Override
    public Actions determineAcceptability() {
        // Check if we are in the first phase or the second phase.
        if (negotiationSession.getTimeline().getTime() < 0.9) {
            if (AC_next() || AC_firstPhase()) {
                return Actions.Accept;
            }
        }
        else {
            if (AC_next() || AC_secondPhase() || AC_time()) {
                return Actions.Accept;
            }
        }
        return Actions.Reject;
    }

    /**
     * Returns the minimum utility our agent desires at a certain moment in the second phase.
     * @return the minimum desired utility.
     */
    private double minimumUtilityPhase2()  {
        return 0.4 + 0.45 * Math.exp(negotiationSession.getTimeline().getTime() * 15.5 - 13.95);
    }

    /**
     * Checks and compares our utility of the opponent's bid with our minimum desired utility.
     * @return Whether the opponent's bid yields us a utility higher than our desired minimum.
     */
    private boolean AC_secondPhase() {
        return negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil() >= minimumUtilityPhase2();
    }

    /**
     * Checks and compares our utility of the opponent's bid with a constant alpha.
     * @return Whether the opponent's bid yields us a higher utility than alpha.
     */
    private boolean AC_firstPhase() {
        return negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil() >= alpha;
    }

    /**
     * Checks whether it's very late in the negotiation.
     * Does also check whether the opponent's bid is better than the reservation value.
     * @return Whether it's very late in the negotiation and the agent should probably accept before it ends up with nothing.
     */
    private boolean AC_time() {
        return negotiationSession.getTimeline().getTime() >= 0.995 && negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil() > negotiationSession.getUtilitySpace().getReservationValue();
    }

    /**
     * Checks whether the utility of the opponent's bid for us is higher than our own next bid.
     * @return Whether the utility of the opponent's bid for us is higher than our own next bid.
     */
    private boolean AC_next() {
        double nextBidUtil = offeringStrategy.getNextBid().getMyUndiscountedUtil();
        double opponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil();
        return opponentBidUtil >= nextBidUtil;
    }

    @Override
    public String getName() {
        return "Acceptance strategy group 30";
    }
}