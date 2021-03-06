package net.coderodde;

import java.util.Arrays;
import java.util.Scanner;

public final class DigitDecoder {

    private static final int NOT_USED = -1;

    public static int[] compute(String s) {
        char[] chars           = s.toCharArray();
        int[] outputArray      = new int[26];
        int currentIndex       = 0;
        char char1             = 0;
        char char2             = 0;
        boolean lastCharIsHash = false;
        boolean readingCount   = false;
        int outputArrayIndex   = NOT_USED;
        int count              = NOT_USED;

        while (currentIndex < chars.length) {
            char c = chars[currentIndex];

            switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    if (lastCharIsHash) {
                        if (currentIndex < 2) {
                            throw new IllegalArgumentException(
                                    "Premature '#'.");
                        }

                        lastCharIsHash = false;
                        count = 1;
                        char1 = c;
                        char2 = 0;
                    } else if (readingCount) {
                        // Just append 'c' to the count variable shifting all 
                        // previous digits one position higher:
                        if (count == NOT_USED) {
                            count = 0;
                        }

                        count *= 10;
                        count += c - '0';
                    } else if (char1 == 0) {
                        char1 = c;
                    } else if (char2 == 0) {
                        char2 = c;
                    } else {
                        // Here, we can safely count the 'char1':
                        outputArrayIndex = char1 - '0' - 1;
                        count = 1;
                        char1 = char2;
                        char2 = c;
                    }
                }
                case '#' -> {
                    if (char1 == 0 || char2 == 0) {
                        throw new IllegalArgumentException(
                                "Missing a character at index "
                                        + currentIndex
                                        + ".");
                    }

                    outputArrayIndex = (char1 - '0') * 10 +
                                       (char2 - '0') - 1;

                    lastCharIsHash = true;
                }
                case '(' -> {
                    if (lastCharIsHash) {
                        lastCharIsHash = false;
                    } else if (char2 == 0) {
                        outputArrayIndex = char1 - '0' - 1;
                    } else {
                        outputArray[char1 - '0' - 1]++;
                        outputArrayIndex = char2 - '0' - 1;
                    }

                    if (currentIndex == 0) {
                        throw new IllegalArgumentException(
                                "'(' cannot be the first characters.");
                    }

                    if (currentIndex > chars.length - 3) {
                        throw new IllegalArgumentException("No closing ')'.");
                    }

                    readingCount = true;
                }
                case ')' -> {
                    if (char2 == 0) {
                        if (char1 == 0) {
                            return outputArray;
                        } else {
                            outputArrayIndex = char1 - '0' - 1;
                        }
                    } else {
                        // Here, char2 not zero:
                        if (char1 == 0) {
                            throw new IllegalArgumentException(
                                    "Should not get here.");
                        }
                    }

                    char1 = 0;
                    char2 = 0;
                    readingCount = false;
                }
                default -> throw new IllegalArgumentException(
                        "Unknown character: " + c);
            }

            currentIndex++;

            if (!readingCount 
                    && outputArrayIndex != NOT_USED 
                    && count != NOT_USED) {
                outputArray[outputArrayIndex] += count;
                outputArrayIndex = NOT_USED;
                count = NOT_USED;
            }
        }

        // Discharge the character buffer leftovers:
        if (char2 != 0) {
            if (char1 == 0) {
                throw new IllegalStateException(
                        "Missing first character in the buffer.");
            }

            // Once here, both the digits are in the buffer:
            if (lastCharIsHash) {
                int index = (char1 - '0') * 10 + char2 - '0' - 1;
                outputArray[index]++;
            } else {
                int index1 = char1 - '0' - 1;
                int index2 = char2 - '0' - 1;
                outputArray[index1]++;
                outputArray[index2]++;
            }
        } else if (char1 != 0) {
            // Only first character in the buffer left:
            outputArray[char1 - '0' - 1]++;
        }

        return outputArray;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int[] decodedInts1 = null;
        int[] decodedInts2 = null;

        while (true) {
            String code = scanner.nextLine();

            if (code.trim().equals("q")) {
                System.out.println("Bye!");
                return;
            }

            boolean noCompare = false;

            try {
                decodedInts1 = compute(code);
                System.out.println("cr: " + Arrays.toString(decodedInts1));
            } catch (Throwable t) {
                System.err.println(
                        "coderodde's implementation failed: " 
                                + t.getMessage() + ".");

                noCompare = true;
            }

            try {
                decodedInts2 = OPDigitDecoder.frequency(code);
                System.out.println("OP: " + Arrays.toString(decodedInts2));
            } catch (Throwable t) {
                System.err.println(
                        "OP's implementation failed: " 
                                + t.getMessage() + ".");

                noCompare = true;
            }

            if (!noCompare) {
                System.out.println(
                        "Algorithms agree: " 
                                + Arrays.equals(decodedInts1, decodedInts2));
            }
        }
    }
}
