package com.sprinthive.pokerhands;

import com.sprinthive.pokerhands.exception.NotEnoughCardsInDeckException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
//This line declares a new Java class named Deck. This class will contain the functionality for managing a deck of playing cards.
    private  List<Card> cards = new ArrayList<>(52);// NB removed the word cards
/*Here, a private instance variable named cards is declared.
 It's a list (ArrayList) that will hold Card objects. Initially, it's set to an empty ArrayList with an initial capacity of 52.*/
    public Deck() {
        //This is the constructor method for the Deck class. It's called when a new Deck object is created.
        for (Suit suit : Suit.values()) {
            for (CardRank rank : CardRank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        /*This code fills the cards list with all possible combinations of playing cards.
         It iterates over each Suit and each CardRank, creating a Card object for each combination and adding it to the cards list.*/

        Collections.shuffle(cards);
    }
    /*After all cards are added to the cards list, this line shuffles the cards randomly, so they're in a random order.
    This simulates shuffling a real deck of cards.*/

    public synchronized int getNumberOfCards() {
        /*This line declares a method named getNumberOfCards, which returns the number of cards in the deck.
     The synchronized keyword means that only one thread can execute this method at a time, which can be important in multithreaded programs to prevent conflicts.*/
        return cards.size();
        /*This line simply returns the size of the cards list, which corresponds to the number of cards in the deck.*/
    }


    public synchronized Card[] pick(int numberOfCards) throws NotEnoughCardsInDeckException {
        /*This line declares a method named pick, which allows picking a specified number of cards from the deck.
         It takes an integer parameter numberOfCards indicating how many cards to pick.
         It also specifies that this method may throw a NotEnoughCardsInDeckException if there aren't enough cards left in the deck.
         The synchronized keyword ensures that only one thread can execute this method at a time.
         */
        if(numberOfCards > cards.size()){
            throw new IllegalArgumentException("Number of cards to pick from a deck must be 52 or less.");
        }
        Card[] picked = new Card[numberOfCards];
        for (int i = 0; i < numberOfCards; i++) {
            picked[i] = cards.remove(cards.size() - 1);
        }
        /*This code checks if the requested number of cards to pick is greater than the total number of cards in the deck (52).
         If it is, it throws an IllegalArgumentException with an error message.*/
        //Todo: This method still needs to be implemented
        return picked;
        /*Currently, this line returns null, indicating that the method doesn't yet return an array of Card objects as intended.
         This part of the code is yet to be implemented.*/
    }
}
