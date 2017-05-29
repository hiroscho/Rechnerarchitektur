package prakt3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CacheSim {
	// indexLength = s;
	private static int indexLength, E, b;
	private static int tagLength, offset;
	private static String t;
	private static boolean v;
	private static String[][] cache;
	private static int[][][] lru;
	private static int[] score = new int[3];

	public static void main(String[] args) {
		handleArguments(args);
		// TODO: not sure if it's the offset (block bits wth)
		offset = b + 2;

		tagLength = 64 - indexLength - offset;
		// [set][block]
		cache = new String[(int) Math.pow(2, indexLength)][E];

		// [set][][]
		lru = new int[(int) Math.pow(2, indexLength)][E][E];
		// startSim();
	}

	private static void hit() {
		score[0]++;
	}

	private static void miss() {
		score[1]++;
	}

	private static void evict() {
		score[2]++;
	}

	// it works but the lru data is seperated, is this right then?
	private static void updateLRU(int setIndex, int blockIndex) {
		for (int i = 0; i < E; i++) {
			lru[setIndex][blockIndex][i] = 1;
		}
		for (int i = 0; i < E; i++) {
			lru[setIndex][i][blockIndex] = 0;
		}
	}

	private static int getLRU(int setIndex) {
		if (E == 1) {
			return 0;
		}		
		for (int row = 0; row < E; row++) {
			for (int col = 0; col < E; col++) {
				if (lru[setIndex][row][col] != 0) {
					break;
				}
				if (col == E - 1) {
					return row;
				}
			}
		}
		return -1;
	}

	private static void startSim() {
		try (BufferedReader br = new BufferedReader(new FileReader(new File(t)))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(" S ") || line.startsWith(" L ") || line.startsWith(" M ")) {
					String op = line.substring(1, 2);
					String[] temp = line.substring(3).split(",");
					String binAddr = getBinaryAddress(temp[0]);
					int size = Integer.parseInt(temp[1]);

					switch (op) {
					case "S":

						break;
					case "L":
						int setIndex = Integer.parseInt(binAddr.substring(tagLength, tagLength + indexLength), 2);
						// cache wouldn't do this sequentially
						for (int i = 0; i < cache[setIndex].length; i++) {
							String block = cache[setIndex][i];

							// do we need a valid bit if we have a perfect lru?
							// valid check
							if (block.substring(0, 1).equals("1")) {
								// tag check
								if (block.equals(binAddr.substring(1, tagLength + 1))) {
									hit();
									updateLRU(setIndex, i);
									// read the data and do smth with it
									// data = block.substring(tagLength + 1);
									break;
								}
								// we've got a miss, overwrite lru
								if (i == cache[setIndex].length - 1) {
									miss();

									// TODO: LRU algo
									// log2(E) ist die anzahl an nötigen bits
									// für
									// LRU
									// nutze nxn matrix für lru, muss ichs noch
									// in cache schreiben?

									setCache(setIndex, getLRU(setIndex), binAddr);
									break;
								}
							} else { // invalid block
								miss();
								setCache(setIndex, i, binAddr);
							}
						}
						break;
					case "M":

						break;
					default:
						throw new Exception(String.format("%s isn't a valid operation!", op));
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private static void setCache(int set, int block, String biAddr) {
		cache[set][block] = "1" + biAddr.substring(1, tagLength + 1)/* + data */;
		updateLRU(set, block);
	}

	private static String getBinaryAddress(String hexAddr) {
		return String.format("%64s", Long.toBinaryString(Long.parseLong(hexAddr, 16))).replace(" ", "0");
	}

	private static void handleArguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-s":
				i++;
				try {
					indexLength = Integer.parseInt(args[i]);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				break;

			case "-E":
				i++;
				try {
					E = Integer.parseInt(args[i]);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				break;

			case "-b":
				i++;
				try {
					b = Integer.parseInt(args[i]);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				break;

			case "-t":
				i++;
				t = args[i];
				break;

			case "-v":
				v = true;
				break;

			case "-h":
				System.out.println("-s <s> Anzahl der Indexbits s, 2^s ist die Anzahl der Cache Blöcke\n"
						+ "-E <E> Assoziativität des Caches, E = Anzahl der Blöcke pro Satz\n"
						+ "-b <b> Anzahl der Block Bits, 2^b ist die Blockgröße\n"
						+ "-t <tracefile> der Name der valgrind Trace Datei, die der Simulator simulieren soll\n"
						+ "-v aktiviert den „verbose“ Mode, Cachesimulator gibt für jede eingelesene Trace Zeile das aktuelle Cache Verhalten  aus\n"
						+ "-h gibt diese Hilfe aus");
				System.exit(0);
			}
		}
	}
}
