package Evil_Code_Renewable;

public class Fraction{
	int numer, denom;
	Fraction(int a, int b){numer=a; denom=b;}
	public static int GCD(int a, int b){return b == 0 ? a : GCD(b, a % b);}
	public static int LCM(int a, int b){return (a * b) / GCD(a, b);}
	void add(int a, int b){
		if(b != denom){
			int new_denom = LCM(denom, b);
			numer *= (new_denom / denom);
			a *= (new_denom / b);
			denom = new_denom;
		}
		numer += a;
	}
	int take1s(){
		int whole = numer / denom;
		numer %= denom;
		return whole;
	}

	@Override public String toString(){
		return numer+"/"+denom;
	}

	public static Fraction fromString(String str){
		int i = str.indexOf('/');
		if(i == -1) return null;
		try{
			return new Fraction(Integer.parseInt(str.substring(0, i)), Integer.parseInt(str.substring(i+1)));
		}
		catch(NumberFormatException ex){
			return null;
		}
	}
}