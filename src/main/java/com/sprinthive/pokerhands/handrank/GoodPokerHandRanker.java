package com.sprinthive.pokerhands.handrank;
import com.sprinthive.pokerhands.Card;
import com.sprinthive.pokerhands.CardRank;
import com.sprinthive.pokerhands.Suit;
import com.sprinthive.pokerhands.exception.NotEnoughCardsInDeckException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BetterPokerHandRanker implements HandRanker {

    public HandRank findBestHandRank(List<Card> cards) {
        if (cards.size() != 5) {
            return new NotRankableHandRanker(cards);
        }

        if (cards.size() == 0) {
            throw new NotEnoughCardsInDeckException("Not enough cards");
        }

        if (isRoyalFlush(Suit.DIAMONDS, cards)) {
            return new RoyalFlushHandRank(Suit.DIAMONDS);
        }

        if (isRoyalFlush(Suit.SPADES, cards)) {
            return new RoyalFlushHandRank(Suit.SPADES);
        }

        if (isRoyalFlush(Suit.CLUBS, cards)) {
            return new RoyalFlushHandRank(Suit.CLUBS);
        }

        if (isRoyalFlush(Suit.HEARTS, cards)) {
            return new RoyalFlushHandRank(Suit.HEARTS);
        }

        if (isStraightFlush(cards)) {
            CardRank highCardRank = findHighCard(cards);
            return new StraightFlushHandRank(highCardRank);
        }

        if (isFlush(Suit.CLUBS, cards)) {
            return new FlushHandRank(cards);
        }

        if (isFlush(Suit.HEARTS, cards)) {
            return new FlushHandRank(cards);
        }

        if (isFlush(Suit.SPADES, cards)) {
            return new FlushHandRank(cards);
        }

        if (isFlush(Suit.DIAMONDS, cards)) {
            return new FlushHandRank(cards);
        }

        if (isFullHouseHandRank(cards)) {
            CardRank[] ranks = getFullHouseRanks(cards);
            return new FullHouseHandRank(ranks[0], ranks[1]);
        }

        KindOfHand value = isThreeOrFourOfAKind(cards);
        if (value != null) {
            if (value.getCount() == 3) {
                return new ThreeOfAKindHandRank(value.getNumber());
            }

            if (value.getCount() == 4) {
                return new FourOfAKindHandRank(value.getNumber());
            }
        }

        if (isStraightAceHigh(cards)) {
            return new StraightHandRank(CardRank.ACE); // Ace high
        }

        if (isStraight(cards)) {
            CardRank highCardRank = findHighCard(cards);
            return new StraightHandRank(highCardRank);
        }

        Map<CardRank, Integer> rankCounts = new HashMap<>();
        for (Card card : cards) {
            rankCounts.put(card.getRank(), rankCounts.getOrDefault(card.getRank(), 0) + 1);
        }

        if (rankCounts.size() == 2) {
            CardRank trips = null;
            CardRank pair = null;
            for (Map.Entry<CardRank, Integer> entry : rankCounts.entrySet()) {
                if (entry.getValue() == 3) {
                    trips = entry.getKey();
                } else if (entry.getValue() == 2) {
                    pair = entry.getKey();
                }
            }
            if (trips != null && pair != null) {
                return new FullHouseHandRank(trips, pair);
            }
        }

        if (rankCounts.size() == 3) {
            List<CardRank> pairs = new ArrayList<>();
            CardRank kicker = null;
            for (Map.Entry<CardRank, Integer> entry : rankCounts.entrySet()) {
                if (entry.getValue() == 2) {
                    pairs.add(entry.getKey()); // Add pair cards to the pairs list
                } else {
                    kicker = entry.getKey(); // Save the kicker card
                }
            }
            if (pairs.size() == 2 && kicker != null) {
                return new TwoPairHandRank(pairs.get(1), pairs.get(0), kicker);
            }
        }

        if (rankCounts.size() == 3) {
            CardRank highPair = null;
            CardRank lowPair = null;
            CardRank kicker = null;
            for (Map.Entry<CardRank, Integer> entry : rankCounts.entrySet()) {
                if (entry.getValue() == 2) {
                    if (highPair == null) {
                        highPair = entry.getKey();
                    } else if (lowPair == null) {
                        lowPair = entry.getKey();
                    }
                } else {
                    kicker = entry.getKey();
                }
            }
            if (highPair != null && lowPair != null && kicker != null) {
                return new TwoPairHandRank(highPair, lowPair, kicker);
            }
        }

        if (rankCounts.size() == 4) {
            CardRank pairRank = null;
            List<CardRank> secondParameter = new ArrayList<>(); // Dummy list for the rest parameter
            for (Map.Entry<CardRank, Integer> entry : rankCounts.entrySet()) {
                if (entry.getValue() == 2) {
                    pairRank = entry.getKey();
                } else {
                    secondParameter.add(entry.getKey()); // Add the non-pair cards to the dummy list
                }
            }
            if (pairRank != null) {
                return new OnePairHandRank(pairRank, secondParameter);
            }
        }

        // No specific hand found, return the high card hand
        Collections.sort(cards, Collections.reverseOrder());
        return new HighCardHandRank(cards);
    }

    private boolean isStraightAceHigh(List<Card> cards) {
        Collections.sort(cards);
        // Check if it's a straight with Ace high
        return cards.get(0).getRank() == CardRank.TEN &&
                cards.get(1).getRank() == CardRank.JACK &&
                cards.get(2).getRank() == CardRank.QUEEN &&
                cards.get(3).getRank() == CardRank.KING &&
                cards.get(4).getRank() == CardRank.ACE;
    }

    private boolean isStraight(List<Card> cards) {
        Collections.sort(cards);
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRank().getValue() != cards.get(i + 1).getRank().getValue() - 1) {
                return false;
            }
        }
        return true;
    }

    private CardRank findHighCard(List<Card> cards) {
        Collections.sort(cards);
        return cards.get(cards.size() - 1).getRank(); // The last card after sorting has the highest rank
    }

    private KindOfHand isThreeOrFourOfAKind(List<Card> cards) {
        Map<CardRank, Integer> rankCounts = new HashMap<>();
        for (Card card : cards) {
            rankCounts.put(card.getRank(), rankCounts.getOrDefault(card.getRank(), 0) + 1);
        }
        for (Map.Entry<CardRank, Integer> entry : rankCounts.entrySet()) {
            if (entry.getValue() == 3 || entry.getValue() == 4) {
                return new KindOfHand(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }
    private boolean isStraightFlush(List<Card> cards) {
        if (isStraightFlushWithSuit(Suit.CLUBS, cards) ||
                isStraightFlushWithSuit(Suit.HEARTS, cards) ||
                isStraightFlushWithSuit(Suit.SPADES, cards) ||
                isStraightFlushWithSuit(Suit.DIAMONDS, cards)) {
            // It's a straight flush with any suit
            return true;
        } else {
            // It's not a straight flush with any suit
            return false;
        }
    }

    private boolean isStraightFlushWithSuit(Suit suit, List<Card> cards) {
        if (isFlush(suit, cards) && isStraightWithSuit(suit, cards)) {
            // It's a straight flush with the given suit
            return true;
        }
        return false;
    }

    private boolean isStraightWithSuit(Suit suit, List<Card> cards) {
        List<Card> filteredCards = cards.stream()
                .filter(card -> card.getSuit() == suit)
                .collect(Collectors.toList());

        // Check if the filtered cards form a straight
        return isStraight(filteredCards);
    }

    private boolean isFlush(Suit suit, List<Card> cards) {
        AtomicBoolean allSameSuit = new AtomicBoolean(true);

        cards.forEach(card -> {
            // Check if all cards have the same suit
            if (card.getSuit() != suit) {
                allSameSuit.set(false);
            }
        });

        return allSameSuit.get();
    }

    private boolean isFullHouseHandRank(List<Card> cards) {
        Map<CardRank, Integer> rankCount = new HashMap<>();
        for (Card card : cards) {
            rankCount.put(card.getRank(), rankCount.getOrDefault(card.getRank(), 0) + 1);
        }
        if (rankCount.size() == 2) {
            int count1 = rankCount.values().iterator().next();
            int count2 = rankCount.values().stream().skip(1).findFirst().get();
            return (count1 == 3 && count2 == 2) || (count1 == 2 && count2 == 3);
        }
        return false;
    }

    private CardRank[] getFullHouseRanks(List<Card> cards) {
        Map<CardRank, Integer> rankCount = new HashMap<>();
        for (Card card : cards) {
            rankCount.put(card.getRank(), rankCount.getOrDefault(card.getRank(), 0) + 1);
        }
        CardRank[] ranks = new CardRank[2];
        for (Map.Entry<CardRank, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 3) {
                ranks[0] = entry.getKey(); // Triplet rank
            } else if (entry.getValue() == 2) {
                ranks[1] = entry.getKey(); // Pair rank
            }
        }
        return ranks;
    }

    private boolean isRoyalFlush(Suit suit, List<Card> royalCards) {
        if (isFlush(suit, royalCards)) {
            Map<CardRank, Integer> rankCounts = new HashMap<>();
            for (Card card : royalCards) {
                rankCounts.put(card.getRank(), rankCounts.getOrDefault(card.getRank(), 0) + 1);
            }

            if (rankCounts.containsKey(CardRank.ACE) &&
                    rankCounts.containsKey(CardRank.KING) &&
                    rankCounts.containsKey(CardRank.QUEEN) &&
                    rankCounts.containsKey(CardRank.JACK) &&
                    rankCounts.containsKey(CardRank.TEN)
            ) {
                return true;
            }
        }
        return false;
    }
}