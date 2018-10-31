import java.util.*;
import java.net.*;
import java.io.*;

/*
 TO DO LIST
 - More println() server commands
 - Bug testing
 - Server can kick players
 - Tell user exact amounts of spending and earning for all actions
 - More world territory?
 IDEAS LIST
 - Different territory zones
 - Slower text output (allow user to read)
*/

public class HexWorldServer {
	// These variables are accessible to all ServerThreads. These are global server
	// information.

	// All connected sockets.
	public static ArrayList<Socket> sockets = new ArrayList<Socket>();

	// All empires in the game, the players.
	public static ArrayList<Empire> empires = new ArrayList<Empire>();

	// Available land.
	public static int openLand = 1000;

	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(6000);

		try {
			while (true) {
				new ServerThread(server.accept()).start();
			}
		} catch (Exception e) {
			println("[!] UNEXPECTED CRASH : " + e);
		}
	}

	private static class ServerThread extends Thread {
		// The bound socket.
		private Socket socket;

		// The printwriter for this socket.
		static private PrintWriter printer;

		// The reader for this socket.
		static private BufferedReader reader;

		// User input is stored here if it will be re-read later.
		private String in;

		public ServerThread(Socket s) {
			try {
				socket = s;
				sockets.add(s);
				printer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				println(" >  ServerThread " + s.toString() + " created.");
			} catch (Exception e) {
				println("[!] ServerThread " + s.toString() + " Failed to create!");
			}
		}

		// Use sendSvr(o), sendErr(o), or send(o) to send messages to user.
		// Use getInput() to get the next line of user input.

		// The tutorial.
		public void tutorial() {
			println(" >  Running tutorial for " + socket.toString());
			String[] tutorial = { "Welcome to HexWorld!\n"
					+ "HexWorld is a civilization-building game where you create an empire and grow its power.\n"
					+ "Conquer territory, make alliances, and decide what kind of ruler you are.\n"
					+ "Other empires may be friends or enemies, and you must react quickly and be prepared for anything.\n"
					+ "This tutorial will teach you how to navigate the game and strategize best so you have the best chance possible in HexWorld.\n"
					+ "Good luck! The proper tutorial begins on the next page. Just send the word Next to continue.",
					"First, let's talk input. Almost all input is presented as words in carats : <Word>.\n"
							+ "When you see something in carats, type just the word into your text bar to select that option. \n"
							+ "Correct spelling is always required and correct capitalization is occasionally required.\n"
							+ "So, if you see <Word>, type Word into the text bar.\n"
							+ "Send <Next> to continue.\n",
					"Now for the fun stuff. Turns are measured in hexons, with are equal to three years. One hexon is one turn. Every turn, you pick an action. There are six categories and twelve actions, so two actions per category. The categories are:\n"
							+ "Army: Defend yourself and attack enemies.\n"
							+ "Science: Research better technologies to gain resources faster.\n"
							+ "Production: Gain goods which let you pay for some actions or buy land.\n"
							+ "Diplomacy: Place other empires under Treaty, preventing them from attacking temporarily.\n"
							+ "Growth: Conquer more territory.\n" + "Development: Store more resources.\n"
							+ "Each category also has a matching resource.\n" + "Send <Next> to continue.\n",
					"Every category has a matching, associated resource with its own three-character icon.\n"
							+ "Army: Soldiers {S}\n" + "Science: Data [D]\n" + "Production: Goods (G)\n"
							+ "Diplomacy: Accord #A#\n" + "Growth: Power !P!\n" + "Development: Progress >P>\n"
							+ "Each category has one action that earns the assosciated resource and another that uses it somehow. Some actions may require two or more resource types to function.\n"
							+ "At the beginning, you can only hold 10 of each; but as you grow that will rapidly change.\n"
							+ "Send <Next> to continue.\n",
					"The main goal of the game is to acquire Territory =T=. Territory is limited; there are only 1000 in the world. You can only conquer unowned Territory.\n"
							+ "Each Territory increases the amount of resources you gain from actions, so get lots of it.\n"
							+ "At some point, all the Territory may be controlled. You will have to resort to destroying other empires to loosen their control or buy the land directly.\n"
							+ "Send <Next> to continue.\n",
					"Now for the actions:\n" + "Army\n"
							+ "-Train: Spend Goods to get Soldiers. Soldiers defend your empire and attack others.\n"
							+ "-Fight: Use your Soldiers to attack another empire. If you have more, they lose control of 1/3 of their territory. Regardless of win or lose, 1/3 of your soldiers are lost.\n"
							+ "Science\n" + "-Research: Spend Goods to get Data. Data is used to Discover.\n"
							+ "-Discover: Spend Data to upgrade a category, which increases the amount of resources gained for that category. The price increases for each level.\n"
							+ "Production\n"
							+ "-Produce: Gain Goods for no cost. Goods are used to buy other resources.\n"
							+ "-Trade Deal: Spend Goods, Accord, and Power to buy land directly from another empire. This is unblockable.\n"
							+ "Diplomacy\n"
							+ "-Negotiate: Gain Accord for no cost. Accord is used to Treaty other players or Trade Deal.\n"
							+ "-Treaty: Spend Accord to disallow target player from using Fight for two turns. You must have more Accord than they have Territory.\n"
							+ "Growth\n" + "-Govern: Gain Power for no cost. Power is used to Conquer Territory.\n"
							+ "-Conquer: Gain Territory for every 10 Power you have. Your Power returns to 0 after Conquer is used.\n"
							+ "Development\n"
							+ "-Invest: Spend Goods to get Progress. Progress is used to increase limits.\n"
							+ "-Build: Spend Progress to increase the limit of one resource. The price increases for each limit boost.\n"
							+ "Send <Next> to continue.\n",
					"Every turn, you will pick an action. Then, you wait 5 seconds for time to pass.\n"
							+ "Your actions change how your empire is aligned. There are four bonuses, which are in two sets of opposites.\n"
							+ "-: Warmonger: Fight to gain a Warmonger point. +1 Soldier/Territory when Train is used. (Opposite is Pacifist)\n"
							+ "+: Pacifist: Don't Fight or Train for three turns. +1 Good/Territory when Produce is used. (Opposite is Warmonger)\n"
							+ "=: Involved: Use Negotiate three times in a row. +1 Progress/Territory when Invest is used. (Opposite is Isolationist)\n"
							+ "~: Isolationist: Don't use Negotiate for six turns in a row. +1 Data/Territory when Research is used. (Opposite is Involved)\n"
							+ "Your points are displayed as a series of symbols. 2 Pacifist points plus 3 Involved points equals ++===; 3 Warmonger points only equals ---; 1 Pacifist point and two Isolationist points equals +; etc. \n"
							+ "Gaining opposite points negates what you already have, e.g. if you have 3 Warmonger points and gain 1 Pacifist point the result is 2 Warmonger points (3 - 1 = 2), if you have 1 Involved point and you gain 2 Isolationist points the result is 1 Isolationist point.\n"
							+ "You start with no points. They are valuable, complete their requirements for a good bonus reward. You can only have up to 3 of any single bonus, however.\n"
							+ "Send <Next> to continue.\n",
					"Your basic strategy is usually this:\n" + "1. Get some Territory.\n"
							+ "2. Use Territory to earn Goods.\n" + "3. Use Goods to gain Data and Discover.\n"
							+ "4. Upgrade your actions.\n" + "5. Use Invest and Build to boost limits.\n"
							+ "Step 6 is up to you -- be diplomatic, fight others, conquer lots of land. Depending on how you play you might never use Negotiate or Fight.\n"
							+ "Send <Next> to continue.\n",
					"Whatever you decide to do, I hope you'll enjoy HexWorld.\n"
							+ "If you catch any bugs, tell them to me, MillerHollinger.\n"
							+ "The tutorial ends here. Have fun!"
							+ "Send <Next> to continue.\n"};
			for (int i = 0; i < tutorial.length; i++)
			{
				sendSvr("PAGE " + i + " - - - - - -");
				sendSvr(tutorial[i]);
				String in = "";
				try {
					in = getInput();
				} catch (Exception e) {
				}
				if (!in.equals("Next"))
					sendSvr("Close enough...");
			}
		}

		// The run() function for each thread. The main for each user.
		public void run() {
			// Intro
			// Welcome the user to the game.
			sendSvr("Successfully connected to server!");
			sendSvr("HexWorld v0.9.9");
			sendSvr("   >By Miller Hollinger");
			// Offer a tutorial.
			send("Would you like to read the Tutorial? <Yes>/<No>");
			boolean decided = false;
			try {
				while (!decided) {
					in = getInput();
					if (in.equalsIgnoreCase("yes") || in.equalsIgnoreCase("y")) {
						decided = true;
						sendSvr("Beginning tutorial.");
						// Tutorial.
						Thread.sleep(1000);
						tutorial();

					} else if (in.equalsIgnoreCase("no") || in.equalsIgnoreCase("n")) {
						decided = true;
						sendSvr("Skipping tutorial.");
					} else {
						sendErr("Input not recognized. Please send <Yes> or <No> (without the < > carats).");
					}
				}
			} catch (Exception e) {
				System.out.println("[!] ERROR OCCURED WHILE REQUESTING TUTORIAL");
				sendErr("An unexpected error occurred. Skipping tutorial step to prevent crash.");
			}

			// Creation
			// Ask for a new empire name.
			send("Enter a new Empire name. No profanity please.");

			String myName = ""; // This Empire's name. IMPORTANT.

			boolean validName = false; // Temp boolean for the following loop.
			while (!validName) {
				try {
					myName = getInput();
					if (myName.length() < 18) {
						validName = true;
						// Check that the name is not yet used.
						for (Empire e : empires)
							if (e.getName().equalsIgnoreCase(myName)) {
								validName = false;
								sendErr("This name is already taken. Please enter another name.");
							}
					} else
						sendErr("The given name is too long. Use a name less than 18 characters.");
				} catch (Exception e) {
					System.out.println("[!] ERROR OCCURED WHILE REQUESTING NAME");
					sendErr("An unexpected error occurred. Please restart the program.");
					try {
						println("A player in progress of joining has left the game beforing naming.");
						while (true)
						Thread.sleep(216000000);
					} catch (Exception m) {
					}
				}
			}
			// Set up the player's empire.
			empires.add(new Empire(myName));

			send("Your Empire name is accepted. Welcome to the dawn of civilization.");

			// Find the user some land.
			if (openLand == 0) {
				sendErr("Unfortunately, the entire world has been conquered.");
				sendErr("Now waiting for some land to become available.");
				while (openLand == 0) {
					if (openLand > 0) {
						send("Land opened up. Now capturing 1 =T=.");
						openLand--;
						empires.get(getIndex(myName)).setTerritory(1);
					}
					try {
						Thread.sleep(2000);
						send("Still looking...");
					} catch (Exception e) {
						sendErr("There was an issue trying to get you land. Please restart.");
					}
				}
			} else {
				openLand--;
				empires.get(getIndex(myName)).setTerritory(1);
			}
			send("You have claimed 1 =T= for your people.");
			send("There is " + openLand + " =T= currently unoccupied in the world.");
			send("Good luck!");
			send("# > > > BEGINNING OF TIME > > > > > > > > > > > > #");
			// Main Game
			boolean turnComplete = false;// If this player has used an action. Loops around input until used.
			while (true) {
				turnComplete = false;
				// Tell the player their age.
				empires.get(getIndex(myName)).countAge();
				send("Your empire is now " + empires.get(getIndex(myName)).getAge() + " hexons old.");
				// Player takes their turn.
				// Show the user their status and the action menu.
				send("Current Status of " + myName);
				send("Alignment : " + empires.get(getIndex(myName)).getAlignText() + " / "
						+ empires.get(getIndex(myName)).getAlign());
				send("# - - - Resources - - - #");
				send("  Soldiers {S} : " + empires.get(getIndex(myName)).getSoldiers() + " / "
						+ empires.get(getIndex(myName)).getSoldiersMax());
				send("      Data [D] : " + empires.get(getIndex(myName)).getData() + " / "
						+ empires.get(getIndex(myName)).getDataMax());
				send("     Goods (G) : " + empires.get(getIndex(myName)).getGoods() + " / "
						+ empires.get(getIndex(myName)).getGoodsMax());
				send("    Accord #A# : " + empires.get(getIndex(myName)).getAccord() + " / "
						+ empires.get(getIndex(myName)).getAccordMax());
				send("     Power !P! : " + empires.get(getIndex(myName)).getPower() + " / "
						+ empires.get(getIndex(myName)).getPowerMax());
				send("  Progress >P> : " + empires.get(getIndex(myName)).getProgress() + " / "
						+ empires.get(getIndex(myName)).getProgressMax());
				send("# - - - Territory - - - #");
				send("     Owned =T= : " + empires.get(getIndex(myName)).getTerritory());
				send("Unoccupied =T= : " + openLand);
				send("# - - - Action Menu - - #");
				send(" > LV " + empires.get(getIndex(myName)).getArmyLv() + " ARMY");
				send("   - <Train> : + {S} | - (G) | Spend Goods to earn Soldiers");
				send("   - <Fight> : - {S} | Target Empire loses Territory if overwhelmed");
				send(" > LV " + empires.get(getIndex(myName)).getScienceLv() + " SCIENCE");
				send("   - <Research> : + [D] | - (G) | Spend Goods to earn Data");
				send("   - <Discover> : - [D] | Spend Data to level-up actions");
				send(" > LV " + empires.get(getIndex(myName)).getProductionLv() + " PRODUCTION");
				send("   - <Produce> : + (G) | Gain Goods");
				send("   - <Trade Deal> :  - (G) | - #A# | - !P! | Buy land from an Empire");
				send(" > LV " + empires.get(getIndex(myName)).getDiplomacyLv() + " DIPLOMACY");
				send("   - <Negotiate> : + #A# | Gain Accord");
				send("   - <Treaty> : - #A# | Target Empire cannot use Fight for two turns");
				send(" > LV " + empires.get(getIndex(myName)).getGrowthLv() + " GROWTH");
				send("   - <Govern> : + !P! | Gain Power");
				send("   - <Conquer> : + =T= | - !P! | Spend Power to conquer unowned Territory");
				send(" > LV " + empires.get(getIndex(myName)).getDevelopmentLv() + " PROGRESS");
				send("   - <Invest> : + >P> | - (G) | Spend Goods to gain Progress");
				send("   - <Build> : - >P> | Spend Progress to increase limits");
				send("# - - - - - - - - - - - #");
				int crashes = 0; // Catches when the user quits and ends the thread.
				while (!turnComplete) {
					send("Which action would you like to do?");
					try {
						in = "";
						in = getInput();
					} catch (Exception e) {
						System.out.println("[!] ERROR WHILE GETTING USER " + myName + " TURN ACTION");
						crashes++;
						sendErr("Unexpected error occurred while getting input. Please retry.");
						if (crashes > 2)
							try {
								println("Empire " + myName + " has left the game.");
								while (true) {
									Thread.sleep(500);
									if (empires.get(getIndex(myName)).getTerritory() <= 0) {
										empires.remove(getIndex(myName));
										println("Abandoned Empire " + myName + " was destroyed.");
										this.join();
									}
								}
							} catch (Exception m) {
							}
					}
					// Record and complete action.
					switch (in) {
					// Army
					case "Train":// Train Soldiers
					case "train":
						if (empires.get(getIndex(myName)).getGoods() > 0) {
							send("You have decided to Train Soldiers {S}.");
							if (empires.get(getIndex(myName)).getSoldiers() < empires.get(getIndex(myName))
									.getSoldiersMax()) {
								boolean resolved = false;
								int maxSpend;
								while (!resolved) {
									maxSpend = Math.min(empires.get(getIndex(myName)).getGoods(),
											empires.get(getIndex(myName)).getTerritory());
									send("You have " + empires.get(getIndex(myName)).getGoods() + " Goods (G).");
									send("You control " + empires.get(getIndex(myName)).getTerritory()
											+ " Territory =T=.");
									send("So, you can spend up to " + maxSpend + " Goods (G) on Soldiers {S}.");
									send("How many Goods (G) will you spend?");
									try {
										int toSpend = Integer.parseInt(getInput());
										if (toSpend <= maxSpend && toSpend >= 0) {
											send("Now spending " + toSpend + " (G).");
											// Soldiers += [min(Territory, Goods) * (Army Level + Warmonger Bonus)] ;
											// Goods
											// -= min(Territory, Goods)
											empires.get(getIndex(myName))
													.addSoldiers(toSpend * (empires.get(getIndex(myName)).getArmyLv()
															+ empires.get(getIndex(myName)).warmonger()));
											empires.get(getIndex(myName)).addGoods(-1 * toSpend);
											send("You now have " + empires.get(getIndex(myName)).getSoldiers()
													+ " {S} / " + empires.get(getIndex(myName)).getSoldiersMax());
											turnComplete = true;
											resolved = true;
										} else {
											sendErr("You cannot spend this amount. Try again."); // input error
										}
									} catch (Exception e) {
										sendErr("Please enter a number only. Try again."); // input error
									}
								}
							} else
								sendErr("You already have maximum Soldiers {S}."); // maxed out
						} else
							sendErr("You don't have any Goods (G)! Use Produce to earn Goods (G)."); // cannot afford
						break;
					case "Fight":// Fight
					case "fight":
						// Select another empire
						send("You have decided to Fight another Empire.");
						if (empires.get(getIndex(myName)).getTreatied() == 0) {
							if (empires.get(getIndex(myName)).getSoldiers() > 0) {
								try {
									send("Please enter the name of an Empire to attack.");
									displayEmpires();
									String target = getInput();
									if (exists(target) && !target.equalsIgnoreCase(myName)) {
										// See who has more soldiers
										// Win: Other player loses 1/3 of all their territory, rounded up (NOT instantly
										// captured, instead it goes up for grabs)
										if (empires.get(getIndex(myName)).canDefeat(empires.get(getIndex(target)))) {
											send("Success! The enemy Empire was defeated.");
											send("The enemy lost control of 1/3 of their territory.");
											openLand += empires.get(getIndex(target)).getTerritory() / 3 + 1;
											empires.get(getIndex(target)).addTerritory(
													-1 * (empires.get(getIndex(target)).getTerritory() / 3 + 1));
											empires.get(getIndex(target)).addEnemyAction("Fight - Defeat - " + myName);
										} else // Loss: Enemy keeps territory.
										{
											send("Failure... The enemy defended.");
											send("No territory was lost.");
											empires.get(getIndex(target)).addEnemyAction("Fight - Victory - " + myName);
										}
										// Lose 1/3 of soldiers.
										send("1/3 of your soldiers were lost in the battle.");
										empires.get(getIndex(myName)).addSoldiers(
												-1 * (empires.get(getIndex(myName)).getSoldiers() / 3) - 1);
										turnComplete = true;
									} else {
										if (!target.equalsIgnoreCase(myName))
											sendErr("That Empire does not exist. Try again."); // inexistent empire
										else
											sendErr("You can't attack yourself!");
									}
								} catch (Exception e) {
									sendErr("There was an issue getting input. Please try again."); // input error
								}
							} else
								sendErr("You have no Soldiers {S}! Acquire Soldiers {S} using Train."); // cannot afford
						} else
							sendErr("You are under Treaty for " + empires.get(getIndex(myName)).getTreatied() // treatied
									+ " more turn(s) and cannot Fight this turn!");
						break;
					// Science
					// Research
					case "Research":
					case "research":
						if (empires.get(getIndex(myName)).getGoods() > 0) {
							send("You have decided to Research Data [D].");
							if (empires.get(getIndex(myName)).getData() != empires.get(getIndex(myName)).getDataMax()) {
								boolean resolved = false;
								int maxSpend;
								while (!resolved) {
									maxSpend = Math.min(empires.get(getIndex(myName)).getGoods(),
											empires.get(getIndex(myName)).getTerritory());
									send("You have " + empires.get(getIndex(myName)).getGoods() + " Goods (G).");
									send("You control " + empires.get(getIndex(myName)).getTerritory()
											+ " Territory =T=.");
									send("So, you can spend up to " + maxSpend + " Goods (G) on Data [D].");
									send("How many Goods (G) will you spend?");
									try {
										int toSpend = Integer.parseInt(getInput());
										if (toSpend <= maxSpend && toSpend > 0) {
											send("Now spending " + toSpend + " (G).");
											// Soldiers += [min(Territory, Goods) * (Army Level + Warmonger Bonus)] ;
											// Goods
											// -= min(Territory, Goods)
											empires.get(getIndex(myName))
													.addData(toSpend * (empires.get(getIndex(myName)).getScienceLv()
															+ empires.get(getIndex(myName)).isolationist()));
											empires.get(getIndex(myName)).addGoods(-1 * toSpend);
											send("You now have " + empires.get(getIndex(myName)).getData() + " [D] / "
													+ empires.get(getIndex(myName)).getDataMax());
											turnComplete = true;
											resolved = true;
										} else {
											sendErr("You cannot spend this amount. Try again."); // input error
										}
									} catch (Exception e) {
										sendErr("Please enter a number only. Try again."); // input error
									}
								}
							} else
								sendErr("You already have maximum Data [D]."); // maxed out
						} else
							sendErr("You don't have any Goods (G)! Use Produce to earn Goods (G)."); // cannot afford
						break;
					// Discover
					case "Discover":
					case "discover":
						send("You have decided Discover an action upgrade.");
						if (empires.get(getIndex(myName)).getData() > 0) {
							send("Your Data [D] : " + empires.get(getIndex(myName)).getData() + " / "
									+ empires.get(getIndex(myName)).getDataMax());
							// Select an action to research
							send("What will you research?");
							send("LV " + empires.get(getIndex(myName)).getArmyLv() + " <Army> : "
									+ empires.get(getIndex(myName)).costArray()[0] + " [D]");
							send("LV " + empires.get(getIndex(myName)).getScienceLv() + " <Science> : "
									+ empires.get(getIndex(myName)).costArray()[1] + " [D]");
							send("LV " + empires.get(getIndex(myName)).getProductionLv() + " <Production> : "
									+ empires.get(getIndex(myName)).costArray()[2] + " [D]");
							send("LV " + empires.get(getIndex(myName)).getDiplomacyLv() + " <Diplomacy> : "
									+ empires.get(getIndex(myName)).costArray()[3] + " [D]");
							send("LV " + empires.get(getIndex(myName)).getGrowthLv() + " <Growth> : "
									+ empires.get(getIndex(myName)).costArray()[4] + " [D]");
							send("LV " + empires.get(getIndex(myName)).getDevelopmentLv() + " <Development> : "
									+ empires.get(getIndex(myName)).costArray()[5] + " [D]");
							try {
								// Spend Data for the levelup for that action
								// 1: 10; 2: 50, 3: 250; 4: 1250; 5: 6050; 6: 30250
								// Upgrade the level of that action
								switch (getInput()) {
								case "Army":
								case "army":
									if (empires.get(getIndex(myName)).upgrade(0)) {
										send("Success. Spending [D] to upgrade Army.");
										send("You now have LV " + empires.get(getIndex(myName)).getArmyLv() + " Army.");
										turnComplete = true;
									} else
										sendErr("You cannot afford this upgrade. Use Research to get more [D] and Build to increase limits."); // cannot
																																				// afford
									break;
								case "Science":
								case "science":
									if (empires.get(getIndex(myName)).upgrade(1)) {
										send("Success. Spending [D] to upgrade Science.");
										send("You now have LV " + empires.get(getIndex(myName)).getScienceLv()
												+ " Science.");
										turnComplete = true;
									} else
										sendErr("You cannot afford this upgrade. Use Research to get more [D] and Build to increase limits.");
									break;
								case "Production":
								case "production":
									if (empires.get(getIndex(myName)).upgrade(2)) {
										send("Success. Spending [D] to upgrade Production.");
										send("You now have LV " + empires.get(getIndex(myName)).getProductionLv()
												+ " Production.");
										turnComplete = true;
									} else
										sendErr("You cannot afford this upgrade. Use Research to get more [D] and Build to increase limits.");
									break;
								case "Diplomacy":
								case "diplomacy":
									if (empires.get(getIndex(myName)).upgrade(3)) {
										send("Success. Spending [D] to upgrade Diplomacy.");
										send("You now have LV " + empires.get(getIndex(myName)).getDiplomacyLv()
												+ " Diplomacy.");
										turnComplete = true;
									} else
										sendErr("You cannot afford this upgrade. Use Research to get more [D] and Build to increase limits.");
									break;
								case "Growth":
								case "growth":
									if (empires.get(getIndex(myName)).upgrade(4)) {
										send("Success. Spending [D] to upgrade Growth.");
										send("You now have LV " + empires.get(getIndex(myName)).getGrowthLv()
												+ " Growth.");
										turnComplete = true;
									} else
										sendErr("You cannot afford this upgrade. Use Research to get more [D] and Build to increase limits.");
									break;
								case "Development":
								case "development":
									if (empires.get(getIndex(myName)).upgrade(5)) {
										send("Success. Spending [D] to upgrade Development.");
										send("You now have LV " + empires.get(getIndex(myName)).getDevelopmentLv()
												+ " Development.");
										turnComplete = true;
									} else
										sendErr("You cannot afford this upgrade. Use Research to get more [D] and Build to increase limits.");
									break;
								default:
									sendErr("The upgrade was not typed correctly. Please assure correct spelling and capitalization and retry."); // input
																																					// error
									break;
								}
							} catch (Exception e) {
								sendErr("There was an issue getting input. Please try again."); // input error
							}

						} else
							sendErr("You don't have any Data [D]. Research to earn Data [D]."); // cannot afford
						break;
					// Production
					// Produce
					case "Produce":
					case "produce":
						send("You have decided to Produce Goods (G).");
						if (empires.get(getIndex(myName)).getGoods() != empires.get(getIndex(myName)).getGoodsMax()) {
							// Goods += [Territory * (Produce Level + Pacifist Bonus)]
							empires.get(getIndex(myName))
									.addGoods((empires.get(getIndex(myName)).getProductionLv()
											+ empires.get(getIndex(myName)).pacifist())
											* empires.get(getIndex(myName)).getTerritory());
							send("You now have " + empires.get(getIndex(myName)).getGoods() + " (G) / "
									+ empires.get(getIndex(myName)).getGoodsMax());
							turnComplete = true;
						} else
							sendErr("You already have maximum Goods (G). Pick another action."); // maxed out
						break;
					// Trade Deal
					case "Trade Deal":
					case "trade deal":
						send("You have decided to make a Trade Deal with another Empire.");
						if (empires.get(getIndex(myName)).getGoods() > 10
								&& empires.get(getIndex(myName)).getAccord() > 10
								&& empires.get(getIndex(myName)).getPower() > 10) {
							try {
								// Select another empire
								send("Please enter the name of an Empire to buy =T= from."); // Shouldn't be able to buy
																								// self territory
								displayEmpires();
								String target = getInput();
								// Buy their land
								// Goods, Accord, Power -= min(Goods, Accord, Power); Territory (taken from
								// target empire) += min (Goods, Accord, Power) / 10
								if (exists(target) && !target.equalsIgnoreCase(myName)) {
									int maxBuy = Math.min(
											Math.min(empires.get(getIndex(myName)).getGoods(),
													empires.get(getIndex(myName)).getAccord()),
											empires.get(getIndex(myName)).getPower());
									maxBuy = Math.min(maxBuy, empires.get(getIndex(target)).getTerritory());
									empires.get(getIndex(myName)).addGoods(-1 * maxBuy); // Pay the price.
									empires.get(getIndex(myName)).addAccord(-1 * maxBuy);
									empires.get(getIndex(myName)).addPower(-1 * maxBuy);
									send("You bought " + maxBuy + " =T= from the target Empire.");
									empires.get(getIndex(myName)).addTerritory(maxBuy / 10);
									empires.get(getIndex(target))
											.addEnemyAction("Trade Deal - Lost " + maxBuy + " =T= - " + myName);
									empires.get(getIndex(target)).addTerritory(maxBuy / -10);
									turnComplete = true;
								} else {
									if (!target.equalsIgnoreCase(myName))
										sendErr("That Empire does not exist. Try again."); // inexistent empire
									else
										sendErr("You can't attack yourself!");
								}
							} catch (Exception e) {
								sendErr("There was an issue getting input. Please try again."); // input error
							}
						} else
							sendErr("You don't have enough (G), #A#, or !P!. You require at least 10 of each."); // cannot
																													// afford
						break;
					// Diplomacy
					// Negotiate
					case "Negotiate":
					case "negotiate":
						send("You have chosen to Negotiate with other Empires.");
						if (empires.get(getIndex(myName)).getAccord() < empires.get(getIndex(myName)).getAccordMax()) {
							empires.get(getIndex(myName)).addAccord(empires.get(getIndex(myName)).getTerritory()
									* empires.get(getIndex(myName)).getDiplomacyLv());
							send("You now have " + empires.get(getIndex(myName)).getAccord() + " Accord #A#.");
							turnComplete = true;
							// Accord += [Territory * Diplomacy Level]
						} else
							sendErr("You already have maximum Power !P!. Pick another action."); // maxed out
						break;
					// Treaty
					case "Treaty":
					case "treaty":
						send("You have decided to Treaty another Empire to stop them from using Fight.");
						if (empires.get(getIndex(myName)).getAccord() > 0) {
							try {
								// Select another empire
								send("Please enter the name of an Empire to Treaty.");
								displayEmpires();
								String target = getInput();
								// Check that Accord > Target's Territory
								if (exists(target) && !target.equalsIgnoreCase(myName)) { // You can currently treaty
																							// yourself, also Treaty
																							// should cost more -
																							// testing/balance required.
									if (empires.get(getIndex(target))
											.treaty(empires.get(getIndex(myName)).getAccord())) {
										// Target cannot use Fight for the next two turns
										// Accord -= Target's Territory
										send("Succesfully used Treaty. The other Empire cannot use Fight for two turns.");
										empires.get(getIndex(target)).addEnemyAction("Treaty - " + myName);
										turnComplete = true;
									} else
										sendErr("The opposing Empire controls too much =T=. Weaken their control or gain #A#."); // not
																																	// strong
																																	// enough
								} else {
									if (!target.equalsIgnoreCase(myName))
										sendErr("That Empire does not exist. Try again."); // inexistent empire
									else
										sendErr("You can't attack yourself!");
								}
							} catch (Exception e) {
								sendErr("There was an issue getting input. Please try again."); // input error
							}
						} else
							sendErr("You don't have enough #A#. You require at least 1."); // cannot afford

						break;
					// Growth
					// Govern
					case "Govern":
					case "govern":
						send("You have chosen to Govern your Empire.");
						if (empires.get(getIndex(myName)).getPower() < empires.get(getIndex(myName)).getPowerMax()) {
							empires.get(getIndex(myName)).addPower(empires.get(getIndex(myName)).getTerritory()
									* empires.get(getIndex(myName)).getGrowthLv());
							send("You now have " + empires.get(getIndex(myName)).getPower() + " Power !P!.");
							turnComplete = true;
						} else
							sendErr("You already have maximum Power !P!. Pick another action."); // maxed out
						break;
					// Conquer
					case "Conquer":
					case "conquer":
						send("You have chosen to Conquer Territory");
						if (empires.get(getIndex(myName)).getPower() >= 10) {
							if (openLand > 0) {
								int conquerable = Math.min(empires.get(getIndex(myName)).getPower() / 10, openLand);
								empires.get(getIndex(myName)).addTerritory(conquerable);
								openLand -= conquerable;
								send("You have Conquered a total of " + conquerable + " Territory =T=.");
								send("You now control " + empires.get(getIndex(myName)).getTerritory() + " =T=.");
								empires.get(getIndex(myName)).setPower(0);
								// Territory += Power / 10 (if there is conquerable land); Power = 0
								turnComplete = true;
							} else
								sendErr("There is no land in the world to Conquer!"); // no land
						} else
							sendErr("You need at least 10 Power !P! to conquer land. Use Govern to gain Power !P!."); // cannot
																														// afford

						break;
					// Development
					// Invest
					case "Invest":
					case "invest":
						if (empires.get(getIndex(myName)).getGoods() > 0) {
							send("You have decided to Invest Progress >P>.");
							if (empires.get(getIndex(myName)).getProgress() != empires.get(getIndex(myName))
									.getProgressMax()) {
								boolean resolved = false;
								int maxSpend;
								while (!resolved) {
									maxSpend = Math.min(empires.get(getIndex(myName)).getGoods(),
											empires.get(getIndex(myName)).getTerritory());
									send("You have " + empires.get(getIndex(myName)).getGoods() + " Goods (G).");
									send("You control " + empires.get(getIndex(myName)).getTerritory()
											+ " Territory =T=.");
									send("So, you can spend up to " + maxSpend + " Goods (G) on Progress >P>.");
									send("How many Goods (G) will you spend?");
									try {
										int toSpend = Integer.parseInt(getInput());
										if (toSpend <= maxSpend && toSpend >= 0) {
											send("Now spending " + toSpend + " (G).");
											// Soldiers += [min(Territory, Goods) * (Army Level + Warmonger Bonus)] ;
											// Goods
											// -= min(Territory, Goods)
											empires.get(getIndex(myName)).addProgress(
													toSpend * (empires.get(getIndex(myName)).getDevelopmentLv()
															+ empires.get(getIndex(myName)).involved()));
											empires.get(getIndex(myName)).addGoods(-1 * toSpend);
											send("You now have " + empires.get(getIndex(myName)).getProgress()
													+ " >P> / " + empires.get(getIndex(myName)).getProgressMax());
											turnComplete = true;
											resolved = true;
										} else {
											sendErr("You cannot spend this amount. Try again."); // input error
										}
									} catch (Exception e) {
										sendErr("Please enter a number only. Try again."); // input error
									}
								}
							} else
								sendErr("You already have maximum Progress >P>."); // maxed out
						} else
							sendErr("You don't have any Goods (G)! Use Produce to earn Goods (G)."); // cannot afford
						break;
					// Build
					case "Build":
					case "build":
						send("You are going to Build to increase limits.");
						if (empires.get(getIndex(myName)).getProgress() > 0) {
							send("Your Progress >P> : " + empires.get(getIndex(myName)).getProgress() + " / "
									+ empires.get(getIndex(myName)).getProgressMax());
							// Select an action to research
							send("What will you research?");
							send("Max " + empires.get(getIndex(myName)).getSoldiersMax() + " <Army> : "
									+ empires.get(getIndex(myName)).getSoldiersMax() + " >P>");
							send("Max " + empires.get(getIndex(myName)).getDataMax() + " <Science> : "
									+ empires.get(getIndex(myName)).getDataMax() + " >P>");
							send("Max " + empires.get(getIndex(myName)).getGoodsMax() + " <Production> : "
									+ empires.get(getIndex(myName)).getGoodsMax() + " >P>");
							send("Max " + empires.get(getIndex(myName)).getAccordMax() + " <Diplomacy> : "
									+ empires.get(getIndex(myName)).getAccordMax() + " >P>");
							send("Max " + empires.get(getIndex(myName)).getPowerMax() + " <Growth> : "
									+ empires.get(getIndex(myName)).getPowerMax() + " >P>");
							send("Max " + empires.get(getIndex(myName)).getProgressMax() + " <Development> : "
									+ empires.get(getIndex(myName)).getProgressMax() + " >P>");
							// Spend Progress to increase the limit of that resource
							// 10>50: 10; 50>250: 50; 250>1250: 250; 1250>6250: 1250; 6250>30250: 1250
							try {
								switch (getInput()) {
								case "Army":
								case "army":
									if (empires.get(getIndex(myName)).getSoldiersMax() < 30000) {
										if (empires.get(getIndex(myName)).getProgress() == empires.get(getIndex(myName))
												.getSoldiersMax()) {
											send("Success. Spending >P> to upgrade Soldier {S} maximum.");
											empires.get(getIndex(myName))
													.setSoldiersMax(empires.get(getIndex(myName)).getSoldiersMax() * 5);
											send("You now have Max " + empires.get(getIndex(myName)).getSoldiersMax()
													+ " Soldiers {S}.");
											empires.get(getIndex(myName))
													.addGoods(-1 * empires.get(getIndex(myName)).getSoldiersMax());
											turnComplete = true;
										} else
											sendErr("You cannot afford this upgrade. Use Invest to get more >P>."); // cannot
																													// afford
									} else
										sendErr("You cannot upgrade this maximum any further."); // maxed stat
									break;
								case "Science":
								case "science":
									if (empires.get(getIndex(myName)).getDataMax() < 30000) {
										if (empires.get(getIndex(myName)).getProgress() == empires.get(getIndex(myName))
												.getDataMax()) {
											send("Success. Spending >P> to upgrade Data [D] maximum.");
											empires.get(getIndex(myName))
													.setDataMax(empires.get(getIndex(myName)).getDataMax() * 5);
											send("You now have Max " + empires.get(getIndex(myName)).getSoldiersMax()
													+ " Data [D].");
											empires.get(getIndex(myName))
													.addGoods(-1 * empires.get(getIndex(myName)).getDataMax());
											turnComplete = true;
										} else
											sendErr("You cannot afford this upgrade. Use Invest to get more >P>.");
									} else
										sendErr("You cannot upgrade this maximum any further.");
									break;
								case "Production":
								case "production":
									if (empires.get(getIndex(myName)).getGoodsMax() < 30000) {
										if (empires.get(getIndex(myName)).getProgress() == empires.get(getIndex(myName))
												.getGoodsMax()) {
											send("Success. Spending >P> to upgrade Goods (G) maximum.");
											empires.get(getIndex(myName))
													.setGoodsMax(empires.get(getIndex(myName)).getGoodsMax() * 5);
											send("You now have Max " + empires.get(getIndex(myName)).getGoodsMax()
													+ " Goods (G).");
											empires.get(getIndex(myName))
													.addGoods(-1 * empires.get(getIndex(myName)).getGoodsMax());
											turnComplete = true;
										} else
											sendErr("You cannot afford this upgrade. Use Invest to get more >P>.");
									} else
										sendErr("You cannot upgrade this maximum any further.");
									break;
								case "Diplomacy":
								case "diplomacy":
									if (empires.get(getIndex(myName)).getAccordMax() < 30000) {
										if (empires.get(getIndex(myName)).getProgress() == empires.get(getIndex(myName))
												.getAccordMax()) {
											send("Success. Spending >P> to upgrade Accord #A# maximum.");
											empires.get(getIndex(myName))
													.setAccordMax(empires.get(getIndex(myName)).getAccordMax() * 5);
											send("You now have Max " + empires.get(getIndex(myName)).getAccordMax()
													+ " Accord #A#.");
											empires.get(getIndex(myName))
													.addGoods(-1 * empires.get(getIndex(myName)).getAccordMax());
											turnComplete = true;
										} else
											sendErr("You cannot afford this upgrade. Use Invest to get more >P>.");
									} else
										sendErr("You cannot upgrade this maximum any further.");
									break;
								case "Growth":
								case "growth":
									if (empires.get(getIndex(myName)).getPowerMax() < 30000) {
										if (empires.get(getIndex(myName)).getProgress() == empires.get(getIndex(myName))
												.getPowerMax()) {
											send("Success. Spending >P> to upgrade Power !P! maximum.");
											empires.get(getIndex(myName))
													.setPowerMax(empires.get(getIndex(myName)).getPowerMax() * 5);
											send("You now have Max " + empires.get(getIndex(myName)).getPowerMax()
													+ " Power !P!.");
											empires.get(getIndex(myName))
													.addGoods(-1 * empires.get(getIndex(myName)).getPowerMax());
											turnComplete = true;
										} else
											sendErr("You cannot afford this upgrade. Use Invest to get more >P>.");
									} else
										sendErr("You cannot upgrade this maximum any further.");
									break;
								case "Development":
								case "development":
									if (empires.get(getIndex(myName)).getProgressMax() < 30000) {
										if (empires.get(getIndex(myName)).getProgress() == empires.get(getIndex(myName))
												.getProgressMax()) {
											send("Success. Spending >P> to upgrade Progress >P> maximum.");
											empires.get(getIndex(myName))
													.setProgressMax(empires.get(getIndex(myName)).getProgressMax() * 5);
											send("You now have Max " + empires.get(getIndex(myName)).getProgressMax()
													+ " Progress >P>.");
											empires.get(getIndex(myName))
													.addGoods(-1 * empires.get(getIndex(myName)).getProgressMax());
											turnComplete = true;
										} else
											sendErr("You cannot afford this upgrade. Use Invest to get more >P>.");
									} else
										sendErr("You cannot upgrade this maximum any further.");
									break;
								default:
									sendErr("The upgrade was not typed correctly. Please assure correct spelling and capitalization and retry."); // input
																																					// error
									break;
								}
							} catch (Exception e) {
								sendErr("There was an issue getting input. Please try again."); // input error
							}
						} else
							sendErr("You have no Progress >P>! Use Invest to earn Progress."); // cannot afford
						break;
					default: // Inexistent action entered
						sendErr("This action does not exist. Please assure correct spelling."); // input
																								// error
						break;
					}
				}
				send("You have now completed your turn.");
				empires.get(getIndex(myName)).addMyAction(in);
				// Tally points under Warmonger, Pacifist, Isolationist, and Involved to see if
				// the stance changes.
				ArrayList<String> myRecent = empires.get(getIndex(myName)).getActions();
				if (in.equalsIgnoreCase("Fight") && empires.get(getIndex(myName)).getPacWar() < 3
						&& empires.get(getIndex(myName)).getPacWar() > -3) // Gain Warmonger
																			// point
																			// (-1)
				{
					send("For using Fight this turn, you recieve +1 Warmonger Point (-1 Pacifist Point).");
					empires.get(getIndex(myName)).setPacWar(empires.get(getIndex(myName)).getPacWar() - 1);
				}
				try {
					if (!myRecent.toArray()[myRecent.toArray().length - 1].equals("Fight")
							&& !myRecent.toArray()[myRecent.toArray().length - 1].equals("Train")
							&& !myRecent.toArray()[myRecent.toArray().length - 2].equals("Fight")
							&& !myRecent.toArray()[myRecent.toArray().length - 2].equals("Train")
							&& !myRecent.toArray()[myRecent.toArray().length - 3].equals("Fight")
							&& !myRecent.toArray()[myRecent.toArray().length - 3].equals("Train")
							&& empires.get(getIndex(myName)).getPacWar() < 3) // Gain Pacifist
																				// point
																				// (+1)
					{
						send("For not using Fight or Train in the last 3 turns, you recieve +1 Pacifist Point (-1 Warmonger Point).");
						empires.get(getIndex(myName)).setPacWar(empires.get(getIndex(myName)).getPacWar() + 1);
					}
				} catch (Exception e) {
				}
				try {
					if (!myRecent.toArray()[myRecent.toArray().length - 1].equals("Negotiate")
							&& !myRecent.toArray()[myRecent.toArray().length - 2].equals("Negotiate")
							&& !myRecent.toArray()[myRecent.toArray().length - 3].equals("Negotiate")
							&& empires.get(getIndex(myName)).getInvIso() < 3) // Gain Involved
																				// point
																				// (+1)
					{
						send("For using Negotiate for the last 3 turns, you recieve +1 Involved Point (-1 Isolationist Point).");
						empires.get(getIndex(myName)).setInvIso(empires.get(getIndex(myName)).getInvIso() + 1);
					}
				} catch (Exception e) {
				}
				try {
					if (!myRecent.toArray()[myRecent.toArray().length - 1].equals("Negotiate")
							&& !myRecent.toArray()[myRecent.toArray().length - 2].equals("Negotiate")
							&& !myRecent.toArray()[myRecent.toArray().length - 3].equals("Negotiate")
							&& !myRecent.toArray()[myRecent.toArray().length - 4].equals("Negotiate")
							&& !myRecent.toArray()[myRecent.toArray().length - 5].equals("Negotiate")
							&& !myRecent.toArray()[myRecent.toArray().length - 6].equals("Negotiate")
							&& empires.get(getIndex(myName)).getInvIso() > -3) // Gain
																				// Isolationist
																				// point (-1)
					{
						send("For not using Negotiate for the last 6 turns, you recieve +1 Isolationist Point (-1 Involved Point).");
						empires.get(getIndex(myName)).setInvIso(empires.get(getIndex(myName)).getInvIso() - 1);
					}
				} catch (Exception e) {
				}

				// Display turn results.
				send("Now, the turn results: ");
				// Display other player's stats.
				displayEmpires();
				// Display other player's actions against them : Fight or Trade Deal.
				send("Other Actions on your Empire:");
				ArrayList<String> newRecents = empires.get(getIndex(myName)).getClearRecents();
				for (int i = 0; i < newRecents.size(); i++)
					send(newRecents.get(i));

				// If player has <= 0 Territory, they lose.
				if (empires.get(getIndex(myName)).getTerritory() <= 0) {
					send("YOU HAVE 0 TERRITORY =T= LEFT!");
					send("Unfortunately, your game is over.");
					boolean causeFound = false;
					for (int i = 0; !causeFound; i++) {
						if (newRecents.get(newRecents.size() - i).contains("Trade Deal")) // If last action was Trade
																							// Deal, tell them they were
																							// bought and peacefully
																							// joined the other country.
						{
							causeFound = true;
							send("Another Empire bought your remaining land.");
							// Give a random hint.
							int randHint = new Random().nextInt(3);
							switch (randHint) {
							case 0:
								send("Next time, keep watch on big Empires likely to be able to afford Trade Deals.");
								break;
							case 1:
								send("Consider allying with mega-Empires to prevent this from happening.");
								break;
							case 2:
								send("Maybe wait until the world resets. You'll get them next time!");
								break;
							}
						} else if (newRecents.get(newRecents.size() - i).contains("Fight")) // If last action was Fight
																							// against them, tell them
																							// they were destroyed.
						{
							causeFound = true;
							send("Your Empire was destroyed by another.");
							// Give a random hint.
							int randHint = new Random().nextInt(3);
							switch (randHint) {
							case 0:
								send("Try using Treaty on aggressive Empires to force them to stop fighting.");
								break;
							case 1:
								send("Hint: If you Trade Deal a warring Empire, you can slow their soldier production.");
								break;
							case 2:
								send("Try making lots of soldiers to counter-attack a dangerous Empire next time.");
								break;
							}
						}
						if (i == newRecents.size()) {
							sendErr("The cause of collapse could not be found. Please report this error.");
						}
					}
					try {
						this.interrupt();
					} catch (Exception e) {
						empires.remove(getIndex(myName));
					}
				}

				// Ten-second turn cooldown. Wait ten seconds to take your next move.
				sendSvr("End of turn information finished.");
				sendSvr("Time is passing. Your next turn is in 5 seconds.");
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					println("[!] Error during end of turn delay.");
				}
			}
		}

		// Returns the index of the empire with the given name
		public static int getIndex(String name) {
			for (int i = 0; i < empires.size(); i++)
				if (empires.get(i).getName().equals(name))
					return i;
			return -1;
		}

		public static boolean exists(String name) {
			for (int i = 0; i < empires.size(); i++)
				if (empires.get(i).getName().equals(name))
					return true;
			return false;
		}

		// Method that displays the list of all empires, their territory owned, level,
		// and alignment.
		public static void displayEmpires() {
			send("# - - - Empire List - - - #");
			for (int i = 0; i < empires.size(); i++)
				send(" LV " + empires.get(i).empireLv() + " - " + empires.get(i).getName() + " "
						+ empires.get(i).getAlign() + " : " + empires.get(i).getTerritory() + " =T=");
			send("# - - - - - - - - - - - - #");
		}

		public static String getInput() throws Exception {
			return reader.readLine();
		}

		// Sends a message to the user from the server, using [>].
		public static void sendSvr(Object o) {
			printer.println("[>] " + o.toString());
		}

		// Sends an error message, using [!].
		public static void sendErr(Object o) {
			printer.println("[!] " + o.toString());
		}

		// Sends a generic message. No prefix.
		public static void send(Object o) {
			printer.println(o.toString());
		}
	}

	public static void print(Object obj) {
		System.out.print(obj.toString());
	}

	public static void println(Object obj) {
		System.out.println(obj.toString());
	}
}
