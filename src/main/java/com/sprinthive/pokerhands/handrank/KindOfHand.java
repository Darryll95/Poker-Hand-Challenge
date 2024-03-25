package com.sprinthive.pokerhands.handrank;

import com.sprinthive.pokerhands.CardRank;

public class KindOfHand {
    private CardRank number;
    private int count;

    public KindOfHand(CardRank number, int count) {
        this.number = number;
        this.count = count;
    }
    public CardRank getNumber() {
        return number;
    }

    public int getCount() {
        return count;
    }

}

