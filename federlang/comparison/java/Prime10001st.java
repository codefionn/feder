import java.lang.Math;
import java.util.List;
import java.util.LinkedList;

public class Prime10001st {
	public static final void main (String[] args) {
		List<Long> primes = new LinkedList<>();
		primes.add (new Long (2l));
		primes.add (new Long (3l));
		primes.add (new Long (5l));

		long current_number = 6l;
		while (primes.size () < 10001) {
			if (isPrime (primes, current_number)) {
				primes.add (new Long (current_number));
			}

			current_number++;
		}

		System.out.println ("10001st prime: " + (current_number - 1));
	}

	private static boolean isPrime (List<Long> primes, long num) {
		long max_num = (long) Math.sqrt ((double) num);
		for (Long prime : primes) {
			if (prime.longValue () > max_num) {
				return true;
			}

			if (num % prime.longValue () == 0) {
				return false;
			}
		}

		return true;
	}
}
