import java.util.ArrayList;

public class Empire {
	// This Empire's name.
	private String name;
	
	// This empire's position on war. Positive is pacifist, negative is warmonger.
	// 3 : Peaceful, 2 : Accepting, 1 : Welcoming, 0 : Neutral, -1 : Unfriendly, -2 : Warlike, -3 : Destructive
	private int pacWar;

	// This empire's position on other empires. Positive is involved, negative is isolationist.
	// 3 : Interventionist, 2 : Interfering, 1 : Upstanding, 0 : Neutral, -1 : Lonely, -2: Closed, -3 : Nationalist
	private int invIso;
	
	// Action levels.
	private int armyLv, scienceLv, productionLv, diplomacyLv, growthLv, developmentLv;

	// Quantities of all resources.
	// Resources : {S} Soldiers, [D] Data, (G) Goods, #A# Accord, !P! Power, >P> Progress, =T= Territory
	private int soldiers, data, goods, accord, power, progress, territory;
	
	// Maximum storages of each resource, the actual number (for convenience, later maybe change to a level and method)
	private int soldiersMax, dataMax, goodsMax, accordMax, powerMax, progressMax;
	
	// All of this player's actions.
	private ArrayList<String> myActions;
	
	// All actions against this player.
	private ArrayList<String> enemyActions;
	
	// New actions against this player.
	private ArrayList<String> recent;
	
	// The age of this Empire.
	private int age;
	
	// How many turns this empire is under treaty (cannot use Fight)
	private int treatied;
	
	// Empty constructor.
	public Empire()	{}
	
	// Standard constructor.
	public Empire(String n) {
		name = n;

		pacWar = 0;
		invIso = 0;

		armyLv = 1;
		scienceLv = 1;
		productionLv = 1;
		diplomacyLv = 1;
		growthLv = 1;
		developmentLv = 1;

		soldiers = 0;
		data = 0;
		goods = 0;
		accord = 0;
		power = 0;
		progress = 0;
		territory = 0;

		soldiersMax = 10;
		dataMax = 10;
		goodsMax = 10;
		accordMax = 10;
		powerMax = 10;
		progressMax = 10;
		
		myActions = new ArrayList<String>();
		enemyActions = new ArrayList<String>();
		recent = new ArrayList<String>();
		
		age = 0;
		
		treatied = 0;
	}

	// Attempts to treaty this player. If accord >= territory, it succeeds.
	public boolean treaty(int accord)
	{	
		
		if (accord >= territory)
		{
			treatied += 2;
			return true;
		}
		return false;
	}
	
	// TODO The actual words of this player's alignment.
	public String getAlignText() {
		String out = "";
		
		switch (pacWar) {
		case 3:
			out += "Peaceful";
			break;
		case 2:
			out += "Accepting";
			break;
		case 1:
			out += "Welcoming";
			break;
		case -1:
			out += "Unfriendly";
			break;
		case -2:
			out += "Warlike";
			break;
		case -3:
			out += "Destructive";
			break;
		}

		switch (invIso) {
		case 3:
			out += " Interventionist";
			break;
		case 2:
			out += " Interfering";
			break;
		case 1:
			out += " Upstanding";
			break;
		case -1:
			out += " Lonely";
			break;
		case -2:
			out += " Closed";
			break;
		case -3:
			out += " Nationalist";
			break;
		}
		
		if (out.equals(""))
			out = "Neutral";
		
		return out;
	}
	
	// Text representation of pacWar and invIso, i.e. this empire's alignment.
	public String getAlign() {
		String out = "";
		String adder = "";

		if (pacWar > 0) // Pacifist
			adder = "+";
		else if (pacWar < 0) // Warmonger
			adder = "-";

		for (int i = 0; i < Math.abs(pacWar); i++)
			out += adder;
		adder = "";

		if (invIso > 0) // Involved
			adder = "=";
		else if (invIso < 0) // Isolationist
			adder = "~";

		for (int i = 0; i < Math.abs(invIso); i++)
			out += adder;

		return out;
	}
	
	// Easy comparison method for battles.
	public boolean canDefeat(Empire e) {
		return e.getSoldiers() < soldiers; // i.e. This empire wins
	}
	
	// Gets the assosciated cost for something of the given level (1: 10, 2 : 50, 3 : 250, 4: 1250, 5: 6050, 6: 30250)
	public int ascCost(int lv)
	{
		return (int) (10 * Math.pow(5, lv - 1));
	}
	
	// Indexes : 0 - Army, 1 - Science, 2 - Production, 3 - Diplomacy, 4 - Growth, 5 - Development
	// Levels : 1: 10, 2 : 50, 3 : 250, 4: 1250, 5: 6050, 6: 30250
	// Returns the cost array for this empire.
	public int[] costArray()
	{
		int[] out = new int[6];
		
		out[0] = ascCost(armyLv);
		out[1] = ascCost(scienceLv);
		out[2] = ascCost(productionLv);
		out[3] = ascCost(diplomacyLv);
		out[4] = ascCost(growthLv);
		out[5] = ascCost(developmentLv);
		
		return out;
	}
	
	// Method that upgrades an action if the player can pay, returns false if they can't.
	// 0 - Army, 1 - Science, 2 - Production, 3 - Diplomacy, 4 - Growth, 5 - Development
	public boolean upgrade(int index)
	{
		int[] out = costArray();
		
		if (data >= out[index])
		{
			data -= out[index];
			switch (index) {
			case 0:
				armyLv++;
				break;
			case 1:
				scienceLv++;
				break;
			case 2:
				productionLv++;
				break;
			case 3:
				diplomacyLv++;
				break;
			case 4:
				growthLv++;
				break;
			case 5:
				developmentLv++;
				break;
			}
			return true;
		}
		else
			return false;
	}
	
	// Returns the total level of this empire's upgrades, i.e. the sum of all levels past one. 1 2 3 4 5 6 = 15. Maximum is thus 30.
	public int empireLv()
	{
		return (int) (armyLv + scienceLv + productionLv + diplomacyLv + growthLv + developmentLv) - 6;
	}
	
	// Increases age by 1
	public void countAge()
	{
		age++;
	}
	
	// TODO Bonus getters. +0, +1, +2, or +3.
	public int warmonger()
	{
		switch (pacWar) {
		case -1:
			return 1;
		case -2:
			return 2;
		case -3:
			return 3;
		default:
			return 0;
		}
	}
	
	public int pacifist()
	{
		switch (pacWar) {
		case 1:
			return 1;
		case 2:
			return 2;
		case 3:
			return 3;
		default:
			return 0;
		}
	}
	
	public int isolationist()
	{
		switch (invIso) {
		case -1:
			return 1;
		case -2:
			return 2;
		case -3:
			return 3;
		default:
			return 0;
		}
	}
	
	public int involved()
	{
		switch (invIso) {
		case 1:
			return 1;
		case 2:
			return 2;
		case 3:
			return 3;
		default:
			return 0;
		}
	}
	
	// Getters, adders, and setters.
	public void addEnemyAction(String plus)
	{
		 
		enemyActions.add(plus);
		recent.add(plus);
	}
	
	public void addMyAction(String plus)
	{
		myActions.add(plus);
	}
	
	// Returns recent and then wipes it.
	public ArrayList<String> getClearRecents()
	{
		ArrayList<String> out = recent;
		recent = new ArrayList<String>();
		return out;
	}
	
	public int getSoldiersMax() {
		return soldiersMax;
	}

	public void setSoldiersMax(int soldiersMax) {
		this.soldiersMax = soldiersMax;
	}

	public void addSoldiers(int plus)
	{
		soldiers += plus;
		if (soldiers > soldiersMax)
			soldiers = soldiersMax;
      if (soldiers < 0)
         soldiers = 0;
	}
	
	public int getGoodsMax() {
		return goodsMax;
	}

	public void setGoodsMax(int goodsMax) {
		this.goodsMax = goodsMax;
	}

	public void addGoods(int plus)
	{
		goods += plus;
		if (goods > goodsMax)
			goods = goodsMax;
         if (goods < 0)
         goods = 0;
	}
	
	public int getAccordMax() {
		return accordMax;
	}

	public void setAccordMax(int accordMax) {
		this.accordMax = accordMax;
	}

	public void addAccord(int plus)
	{
		accord += plus;
		if (accord > accordMax)
			accord = accordMax;
         if (accord < 0)
         accord = 0;
	}
	
	public int getPowerMax() {
		return powerMax;
	}

	public void setPowerMax(int powerMax) {
		this.powerMax = powerMax;
	}

	public void addPower(int plus)
	{
		power += plus;
		if (power > powerMax)
			power = powerMax;
         if (power < 0)
         power = 0;
	}
	
	public int getProgressMax() {
		return progressMax;
	}

	public void setProgressMax(int progressMax) {
		this.progressMax = progressMax;
	}

	public void addProgress(int plus)
	{
		progress += plus;
		if (progress > progressMax)
			progress = progressMax;
         if (progress < 0)
         progress = 0;
	}
	
	public int getDataMax() {
		return dataMax;
	}

	public void setDataMax(int dataMax) {
		this.dataMax = dataMax;
	}
	
	public void addData(int plus)
	{
		data += plus;
		if (data > dataMax)
			data = dataMax;
         if (data < 0)
         data = 0;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getAccord() {
		return accord;
	}

	public void setAccord(int accord) {
		this.accord = accord;
	}

	public int getTerritory() {
		return territory;
	}

	public void setTerritory(int territory) {
		this.territory = territory;
	}

	public void addTerritory(int plus)
	{
		territory += plus;
		if (territory < 0)
			territory = 0;
	}
	
	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getGoods() {
		return goods;
	}

	public void setGoods(int goods) {
		this.goods = goods;
	}

	public int getSoldiers() {
		return soldiers;
	}

	public void setSoldiers(int soldiers) {
		this.soldiers = soldiers;
	}

	public int getProductionLv() {
		return productionLv;
	}

	public void setProductionLv(int productionLv) {
		this.productionLv = productionLv;
	}

	public int getArmyLv() {
		return armyLv;
	}

	public void setArmyLv(int armyLv) {
		this.armyLv = armyLv;
	}

	public int getScienceLv() {
		return scienceLv;
	}

	public void setScienceLv(int scienceLv) {
		this.scienceLv = scienceLv;
	}

	public int getDiplomacyLv() {
		return diplomacyLv;
	}

	public void setDiplomacyLv(int diplomacyLv) {
		this.diplomacyLv = diplomacyLv;
	}

	public int getGrowthLv() {
		return growthLv;
	}

	public void setGrowthLv(int growthLv) {
		this.growthLv = growthLv;
	}

	public int getDevelopmentLv() {
		return developmentLv;
	}

	public void setDevelopmentLv(int developmentLv) {
		this.developmentLv = developmentLv;
	}

	public int getInvIso() {
		return invIso;
	}

	public void setInvIso(int invIso) {
		this.invIso = invIso;
	}

	public int getPacWar() {
		return pacWar;
	}

	public void setPacWar(int pacWar) {
		this.pacWar = pacWar;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getActions() {
		return myActions;
	}

	public void setActions(ArrayList<String> actions) {
		this.myActions = actions;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getTreatied() {
		return treatied;
	}

	public void setTreatied(int treatied) {
		this.treatied = treatied;
	}

	public ArrayList<String> getRecent() {
		return recent;
	}

	public void setRecent(ArrayList<String> recent) {
		this.recent = recent;
	}
	
}
