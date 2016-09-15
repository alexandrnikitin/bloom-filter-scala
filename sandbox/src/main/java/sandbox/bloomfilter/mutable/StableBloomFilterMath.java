package sandbox.bloomfilter.mutable;

import com.google.common.math.IntMath;

public class StableBloomFilterMath {

    /*

    Original paper: https://webdocs.cs.ualberta.ca/~drafiei/papers/DupDet06Sigmod.pdf

    Glossary:

        N Number of elements in the input stream
        M Total space available in bits
        m Number of cells in the SBF
        Max The value a cell is set to
        d Number of bits allocated per cell
        K Number of hash functions
        k The probability that a cell is set in each iteration
        P Number of cells we pick to decrement by 1 in each iteration
        p The probability that a cell is picked to be decremented by 1 in each iteration
        hi The ith hash function


    NOTES from the original paper:

        we assume that users specify m and the allowable FP rate.

        Max as small as possible

        In practice, we limit our choice of Max to
        1, 3 and 7 (if higher time cost can be tolerated, larger values of Max can be tried similarly).


        In practice, given an
        FPS, the amount of available space and the gap distribu-
        tion of the input data, to set the parameters properly, we
        first establish a constraint for P, which means P can be
        computed based on FPS, m, Max and K; then find the
        optimal values of K for each case of Max(1; 3; 7) by trying
        limited number(<= 10) of values of K on the FN rate for-
        mulas; Last, we estimate the expected number of FNs for
        each candidate value of Max using its corresponding opti-
        mal K and some prior knowledge of the stream, and thus
        choose the optimal value of Max. In the case that no prior
        knowledge of the input data is available, we suggest setting
        Max = 1. The described parameter setting process can be
        implemented within a few lines of codes.

        P =
        P can be
        computed based on FPS, m, Max and K;

        K =
        then find the
        optimal values of K for each case of Max(1; 3; 7) by trying
        limited number(<= 10) of values of K on the FN rate formulas;

        K can be set
        by trying different possible values on the formula we derive
        without considering the input data distribution, assuming
        Max is known.

        Max =
        we estimate the expected number of FNs for
        each candidate value of Max using its corresponding opti-
        mal K and some prior knowledge of the stream, and thus
        choose the optimal value of Max.

        In the case that no prior
        knowledge of the input data is available, we suggest setting Max = 1.

        we show that Max can be set empirically.

    */

    // TODO "m" param can be omitted ie 1 / m is small comparing to 1 / K
    public static double calculateFalsePositive(int K, int Max, int P, int m) {
        return Math.pow(1 - Math.pow(1 / (1 + (1 / (P * (1 / (double) K - 1 / (double) m)))), Max), K);
    }

    public static double calculateFalseNegative(int K, int gap, double k, double p, int Max) {
        return 1 - Math.pow((1 - PR0(gap, k, p, Max)), K);
    }

    public static double PR0(int gap, double k, double p, int Max) {
        double sum = 0;
        for (int l = Max; l <= gap; l++) {
            sum += Pr1(l, Max, k) * Pr2(l, k);
        }

        return sum + Pr1(gap, Max, p) * Pr4(gap, k);
    }

    public static double Pr1(int l, int Max, double k) {
        double sum = 0;
        for (int j = Max; j <= l; j++) {
            sum += IntMath.binomial(l, j) * Math.pow(k, j) * Math.pow((1 - k), l - j);
        }
        return sum;
    }

    public static double Pr2(int l, double k) {
        return Math.pow(1 - k, l) * k;
    }

    public static double Pr4(int gap, double k) {
        return Math.pow(1 - k, gap);
    }

}
