from math import sqrt

def isPrime (primes, num):
    max_num = int (sqrt (num))
    
    for prime in primes:
        if max_num < prime:
            return True
         
        if num % prime == 0:
            return False
    
    print (num)
    return True

primes = [2, 3, 5]
current_num = 6
while len (primes) < 10001:
    if isPrime (primes, current_num):
        primes.append (current_num)
    
    current_num += 1

print ("10001st prime:", (current_num-1))
