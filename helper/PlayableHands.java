package helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import bot.BotState;
import poker.Card;
import poker.CardHeight;
import poker.CardSuit;
import poker.HandHoldem;
import poker.PokerMove;

public class PlayableHands {

	public Card card1;
	public Card card2;
	public int suit;
	public List<Integer> hand;

	/*
	 * Instantiate the the two cards that we were dealt and add them to our
	 * hand. The hand will change during the flop, turn, and river depending on
	 * whether or not the cards on the table improves the hand
	 */

	public PlayableHands(Card card1, Card card2) {
		this.card1 = card1;
		this.card2 = card2;
		if (card1.getSuit() != card2.getSuit()) {
			// 0 denotes not same suit
			this.suit = 0;
		} else if (card1.getHeight() == card2.getHeight()) {
			// will never be same suit
			this.suit = 0;
		} else {
			// 1 denotes same suit
			this.suit = 1;
		}
		hand = new ArrayList<Integer>();
		hand.add(card1.getHeight().ordinal());
		hand.add(card2.getHeight().ordinal());
	}

	/* METHODS FOR TH ACTUAL PRE-FLOP, FLOP, TURN, AND RIVER MOVES */

	public PokerMove preFlopMove(BotState state) {
		// if we are the small blind
		if (state.onButton()) {
			if (wantToPlay()) {
				if (tiers() < 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind());
				} else {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				}
			} else {
				if (state.getAmountToCall() < state.getBigBlind() * 2) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else {
					return new PokerMove(state.getMyName(), "fold", 0);
				}
			}
			// if we are the big blind
		} else {
			if (state.getAmountToCall() > 0) {
				if (wantToPlay()) {
					if (tiers() < 3) {
						return new PokerMove(state.getMyName(), "raise",
								state.getBigBlind());
					} else {
						return new PokerMove(state.getMyName(), "call",
								state.getAmountToCall());
					}
				} else {
					if (state.getAmountToCall() < state.getBigBlind() * 2) {
						return new PokerMove(state.getMyName(), "call",
								state.getAmountToCall());
					} else {
						return new PokerMove(state.getMyName(), "fold", 0);
					}
				}
			} else {
				if (tiers() < 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind());
				} else {
					return new PokerMove(state.getMyName(), "check", 0);
				}
			}
		}
	}

	public PokerMove flopMove(BotState state) {
		addToHand(state.getTable());
		// check hand strength
		int handStrength = checkHand(state);
		// check turn improvement
		double turnImprovement = checkTurn(state, handStrength);
		// if we bet second
		if (state.onButton()) {
			if (state.getAmountToCall() > 0) {
				if (handStrength > 0) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else if (turnImprovement > 15
						&& state.getAmountToCall() < state.getBigBlind() * 2
								+ state.getSmallBlind()) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else {
					return new PokerMove(state.getMyName(), "fold", 0);
				}
			} else {
				if (handStrength <= 1 && turnImprovement > 45) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind() * 2);
				} else if (handStrength > 1) {
					return new PokerMove(state.getMyName(), "check", 0);
				} else {
					return new PokerMove(state.getMyName(), "check", 0);
				}
			}
			// if we bet first
		} else {
			if (state.getAmountToCall() > 0) {
				if (handStrength >= 1 && turnImprovement > 15) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind() * 2);
				} else if (state.getAmountToCall() > state.getBigBlind() * 2
						&& handStrength == 0 && turnImprovement < 45) {
					return new PokerMove(state.getMyName(), "fold", 0);
				} else {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				}
			} else {
				if (handStrength >= 1 && turnImprovement > 45) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind() * 2);
				} else {
					return new PokerMove(state.getMyName(), "check", 0);
				}
			}
		}
	}

	public PokerMove turn(BotState state) {
		addToHand(state.getTable());
		// check hand strength
		int handStrength = checkHand(state);
		// check turn improvement
		double turnImprovement = checkTurn(state, handStrength);
		// if we bet second
		if (state.onButton()) {
			if (state.getAmountToCall() > 0) {
				if (handStrength > 1) {
					return new PokerMove(state.getMyName(), "raise",
							state.getPot() * 2);
				} else if (handStrength == 1 && turnImprovement > 15) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else if (turnImprovement > 45
						&& state.getAmountToCall() < state.getBigBlind() * 2
								+ state.getSmallBlind()) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else {
					return new PokerMove(state.getMyName(), "fold", 0);
				}
			} else {
				 if(handStrength >= 2){
					return new PokerMove(state.getMyName(), "raise",
							state.getPot() * 2);
				} else if (turnImprovement > 45) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind() * 2);
				} else {
					return new PokerMove(state.getMyName(), "check", 0);
				}
			}
			// if we bet first
		} else {
			if (state.getAmountToCall() > 0) {
				if (handStrength > 1) {
					return new PokerMove(state.getMyName(), "raise",
							state.getPot() * 2);
				} else if (handStrength == 1 && turnImprovement > 15) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else if (turnImprovement > 45
						&& state.getAmountToCall() < state.getBigBlind() * 2
								+ state.getSmallBlind()) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else {
					return new PokerMove(state.getMyName(), "fold", 0);
				}
			} else {
				if(handStrength >= 2){
					return new PokerMove(state.getMyName(), "raise",
							state.getPot() * 2);
				} else if (turnImprovement > 45) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind() * 2);
				} else {
					return new PokerMove(state.getMyName(), "check", 0);
				}
			}
		}
	}

	public PokerMove river(BotState state) {
		addToHand(state.getTable());
		// check hand strength
		int handStrength = checkHand(state);
		// if we bet second
		if (state.onButton()) {
			if (state.getAmountToCall() > 0) {
				if (handStrength > 1 && handStrength < 3) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else if (handStrength == 1
						&& state.getAmountToCall() < state.getPot()) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else if (handStrength >= 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getAmountToCall() * 2);
				} else {
					return new PokerMove(state.getMyName(), "fold", 0);
				}
			} else {
				if (handStrength > 1 && handStrength < 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind() * 2);
				} else if (handStrength == 1) {
					return new PokerMove(state.getMyName(), "check", 0);
				} else if (handStrength >= 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getPot() * 2);
				} else {
					return new PokerMove(state.getMyName(), "check", 0);
				}
			}
			// if we bet first
		} else {
			if (state.getAmountToCall() > 0) {
				if (handStrength > 1 && handStrength < 3) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else if (handStrength == 1
						&& state.getAmountToCall() < state.getPot()) {
					return new PokerMove(state.getMyName(), "call",
							state.getAmountToCall());
				} else if (handStrength >= 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getAmountToCall() * 2);
				} else {
					return new PokerMove(state.getMyName(), "fold", 0);
				}
			} else {
				if (handStrength > 1 && handStrength < 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getBigBlind() * 2);
				} else if (handStrength == 1) {
					return new PokerMove(state.getMyName(), "check", 0);
				} else if (handStrength >= 3) {
					return new PokerMove(state.getMyName(), "raise",
							state.getPot() * 2);
				} else {
					return new PokerMove(state.getMyName(), "check", 0);
				}
			}
		}
	}

	/* HELPER METHODS USED DURING PRE-FLOP */

	public boolean wantToPlay() {
		// if we have a pair play
		if (card1.getHeight().ordinal() == card2.getHeight().ordinal()) {
			return true;
			// if we have an ace play
		} else if (card1.getHeight() == CardHeight.ACE
				|| card2.getHeight() == CardHeight.ACE) {
			return true;
			// if we have same suit connectors play
		} else if ((card1.getHeight().ordinal() - card2.getHeight().ordinal()) <= 2
				&& suit == 1) {
			return true;
			// if we have a king and our other card is at least a seven play
		} else if (card1.getHeight() == CardHeight.KING
				&& card2.getHeight().ordinal() >= CardHeight.SEVEN.ordinal()) {
			return true;
		} else if (card2.getHeight() == CardHeight.KING
				&& card1.getHeight().ordinal() >= CardHeight.SEVEN.ordinal()) {
			return true;
			// if both of our cards are at least a ten play
		} else if (card1.getHeight().ordinal() >= CardHeight.TEN.ordinal()
				&& card2.getHeight().ordinal() >= CardHeight.TEN.ordinal()) {
			return true;
			// otherwise we don't want to play
		} else {
			return false;
		}

	}

	public int tiers() {
		// Check for pair
		if (card1.getHeight().ordinal() == card2.getHeight().ordinal()) {
			// AA, KK,QQ : tier 1
			if (card1.getHeight().ordinal() >= CardHeight.QUEEN.ordinal()) {
				return 1;
				// JJ, TT, 99 : tier 2
			} else if (card1.getHeight().ordinal() >= CardHeight.NINE.ordinal()) {
				return 2;
				// 88, 77 : tier 3
			} else if (card1.getHeight().ordinal() >= CardHeight.SEVEN
					.ordinal()) {
				return 3;
				// 66, 55, 44, 33, 22 : tier 4
			} else {
				return 4;
			}
			// AKs and AK : tier 1
		} else if (hand.contains(CardHeight.ACE.ordinal())
				&& hand.contains(CardHeight.KING.ordinal())) {
			return 1;
			// AQs and AQ: tier 3
		} else if (hand.contains(CardHeight.ACE.ordinal())
				&& hand.contains(CardHeight.QUEEN.ordinal())) {
			return 3;
			// Check for Ace suited
		} else if (hand.contains(CardHeight.ACE.ordinal()) && suit == 1) {
			// AJ, AT, A9, A8 : tier 4
			if (hand.contains(CardHeight.JACK.ordinal())
					|| hand.contains(CardHeight.TEN.ordinal())
					|| hand.contains(CardHeight.NINE.ordinal())
					|| hand.contains(CardHeight.EIGHT.ordinal())) {
				return 4;
				// A7, A6, A5, A4, A3, A2 : tier 5
			} else {
				return 5;
			}
			// KQs and KQ : tier 5
		} else if (hand.contains(CardHeight.QUEEN.ordinal())
				&& hand.contains(CardHeight.KING.ordinal())) {
			return 5;
			// all other hands : tier 6
		} else {
			return 6;
		}
	}

	/* HELPER METHODS USED DURING FLOP */

	public int checkHand(BotState state) {
		if (haveStraightFlush(state.getTable())) {
			return 8;
		} else if (haveFourOfAKind()) {
			return 7;
		} else if (haveFullHouse()) {
			return 6;
		} else if (haveFlush(state.getTable())) {
			return 5;
		} else if (haveStraight()) {
			return 4;
		} else if (haveThreeOfAKind(state.getTable())) {
			return 3;
		} else if (haveTwoPair()) {
			return 2;
		} else if (havePair()) {
			return 1;
		} else {
			return 0;
		}
	}

	public boolean haveStraightFlush(Card[] table) {
		CardSuit suitt = table[0].getSuit();
		for (int i = 1; i < table.length; i++) {
			if (table[i].getSuit() != suitt) {
				return false;
			}
		}
		if (card1.getSuit() != suitt || card2.getSuit() != suitt) {
			return false;
		}
		for (int i = 1; i < hand.size(); i++) {
			if (hand.get(i - 1) - hand.get(i) != 1) {
				return false;
			}
		}
		return true;
	}

	public boolean haveFourOfAKind() {
		for (int i = 0; i < hand.size(); i++) {
			if (Collections.frequency(hand, hand.get(i)) == 4) {
				return true;
			}
		}
		return false;
	}

	public boolean haveFullHouse() {
		int count3 = 0;
		int count2 = 0;
		for (int i = 0; i < hand.size(); i++) {
			if (Collections.frequency(hand, hand.get(i)) == 3) {
				count3++;
				break;
			}
		}
		for (int i = 0; i < hand.size(); i++) {
			if (Collections.frequency(hand, hand.get(i)) == 2) {
				count2++;
				break;
			}
		}
		if (count3 == 1 && count2 == 1) {
			return true;
		} else {
			return false;
		}
	}

	public boolean haveFlush(Card[] table) {
		int hearts = 0;
		int spades = 0;
		int clubs = 0;
		int diamonds = 0;
		for (int i = 0; i < table.length; i++) {
			String name = table[i].getSuit().toString();
			if (name == "HEARTS") {
				hearts++;
			} else if (name == "SPADES") {
				spades++;
			} else if (name == "CLUBS") {
				clubs++;
			} else {
				diamonds++;
			}
		}
		String name1 = card1.getSuit().toString();
		String name2 = card2.getSuit().toString();
		if (name1 == "HEARTS") {
			hearts++;
		} else if (name1 == "SPADES") {
			spades++;
		} else if (name1 == "CLUBS") {
			clubs++;
		} else {
			diamonds++;
		}
		if (name2 == "HEARTS") {
			hearts++;
		} else if (name2 == "SPADES") {
			spades++;
		} else if (name2 == "CLUBS") {
			clubs++;
		} else {
			diamonds++;
		}
		if (hearts == 5 || spades == 5 || clubs == 5 || diamonds == 5) {
			return true;
		} else {
			return false;
		}
	}

	public boolean haveStraight() {
		int counter = 0;
		for (int i = 1; i < hand.size(); i++) {
			if (hand.get(i - 1) - hand.get(i) == 1) {
				counter++;
			}
		}
		if (counter == 5) {
			return true;
		} else {
			return false;
		}
	}

	public boolean haveThreeOfAKind(Card[] table) {
		//Card[] table = state.getTable();
		if (table.length == 3) {
			if (Collections.frequency(hand, hand.get(0)) != 3
					&& Collections.frequency(hand, hand.get(2)) != 3) {
				return false;
			} else {
				return true;
			}
		} else if (table.length == 4) {
			if (Collections.frequency(hand, hand.get(0)) != 3
					&& Collections.frequency(hand, hand.get(2)) != 3
					&& Collections.frequency(hand, hand.get(3)) != 3) {
				return false;
			} else {
				return true;
			}
		} else if (table.length == 5) {
			if (Collections.frequency(hand, hand.get(0)) != 3
					&& Collections.frequency(hand, hand.get(2)) != 3
					&& Collections.frequency(hand, hand.get(4)) != 3) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public boolean haveTwoPair() {
		int counter = 0;
		for (int i = 0; i < hand.size(); i = i + 2) {
			if (Collections.frequency(hand, hand.get(i)) == 2) {
				counter++;
			}
		}
		if(counter >= 2){
			return true;
		} else {
			return false;
		}
	}

	public boolean havePair() {
		for (int i = 0; i < hand.size(); i++) {
			if (Collections.frequency(hand, hand.get(i)) == 2) {
				return true;
			}
		}
		return false;
	}

	public double checkTurn(BotState state, int handRank) {
		int cardsLeft = 52 - state.getTable().length - 2;
		if (handRank == 8) {
			return 0.0;
		} else if (handRank == 7) {
			return 0.0;
		} else if (handRank == 6) {
			return (1 / cardsLeft) * 7;
		} else if (handRank == 5) {
			return checkStraight() / cardsLeft * 8;
		} else if (handRank == 4) {
			return (checkFlush(state) / cardsLeft) * 5;
		} else if (handRank == 3) {
			return checkThrees(state, cardsLeft);
		} else if (handRank == 2) {
			return checkTwoPair(state, cardsLeft);
		} else if (handRank == 1) {
			return checkPair(state, cardsLeft);
		} else {
			return checkAll(state, cardsLeft);
		}
	}

	public double checkStraight() {
		int counter = 0;
		for (int i = 1; i < hand.size(); i++) {
			if (hand.get(i) - hand.get(i - 1) > 2
					|| hand.get(i) - hand.get(i - 1) == 0) {
				return 0.0;
			} else if (hand.get(i) - hand.get(i - 1) == 2) {
				counter++;
			}
		}
		return (double) counter;
	}

	public double checkFlush(BotState state) {
		Card[] table = state.getTable();
		int hearts = 0;
		int spades = 0;
		int clubs = 0;
		int diamonds = 0;
		for (int i = 0; i < table.length; i++) {
			String name = table[i].getSuit().toString();
			if (name == "HEARTS") {
				hearts++;
			} else if (name == "SPADES") {
				spades++;
			} else if (name == "CLUBS") {
				clubs++;
			} else {
				diamonds++;
			}
		}
		String name1 = card1.getSuit().toString();
		String name2 = card2.getSuit().toString();
		if (name1 == "HEARTS") {
			hearts++;
		} else if (name1 == "SPADES") {
			spades++;
		} else if (name1 == "CLUBS") {
			clubs++;
		} else {
			diamonds++;
		}
		if (name2 == "HEARTS") {
			hearts++;
		} else if (name2 == "SPADES") {
			spades++;
		} else if (name2 == "CLUBS") {
			clubs++;
		} else {
			diamonds++;
		}
		int max = Math.max(hearts, Math.max(spades, Math.max(diamonds, clubs)));
		if (max >= 3) {
			return 13 - max;
		} else {
			return 0.0;
		}
	}

	public double checkThrees(BotState state, int cardsLeft) {
		double flush = (checkFlush(state) / cardsLeft) * 5;
		double straight = (checkStraight() / cardsLeft) * 4;
		double four = (1 / cardsLeft) * 7;
		double full = (((state.getTable().length + 2 - 3) * 3) / cardsLeft) * 6;
		return flush + straight + four + full;
	}

	public double checkTwoPair(BotState state, int cardsLeft) {
		double flush = (checkFlush(state) / cardsLeft) * 5;
		double straight = (checkStraight() / cardsLeft) * 4;
		double full = (4 / cardsLeft) * 6;
		return flush + straight + full;
	}

	public double checkPair(BotState state, int cardsLeft) {
		double flush = (checkFlush(state) / cardsLeft) * 5;
		double straight = (checkStraight() / cardsLeft) * 4;
		double threes = (2 / cardsLeft) * 3;
		double twoPair = (((state.getTable().length + 2 - 2) * 3) / cardsLeft) * 2;
		return flush + straight + threes + twoPair;
	}

	public double checkAll(BotState state, int cardsLeft) {
		double flush = (checkFlush(state) / cardsLeft) * 5;
		double straight = (checkStraight() / cardsLeft) * 4;
		double pair = ((state.getTable().length + 2) * 3) * 1;
		return flush + straight + pair;
	}

	/* UNIVERSALLY USED HELPER METHODS */

	public void addToHand(Card[] table) {
		//Card[] table = state.getTable();
		for(int i = 0; i < table.length; i++){
			hand.add(table[i].getHeight().ordinal());
		}
		Collections.sort(hand);
	}

	public List<Integer> tableToArray(BotState state) {
		Card[] table = state.getTable();
		List<Integer> tableArray = new ArrayList<Integer>();
		for (int i = 0; i < table.length; i++) {
			tableArray.add(table[i].getHeight().ordinal());
		}
		Collections.sort(tableArray);
		return tableArray;
	}

	public PokerMove bluff(BotState state) {
		// bluff method
		return new PokerMove(state.getMyName(), "raise", toRaiseForBluff(state));
	}

	public int toRaiseForBluff(BotState state) {
		// agressive method to determine amount to bluff
		if (card1.getHeight().ordinal() >= CardHeight.TEN.ordinal()
				|| card2.getHeight().ordinal() >= CardHeight.TEN.ordinal()) {
			return state.getPot() * 2;
		} else {
			return state.getPot() + 5;
		}
	}
}
