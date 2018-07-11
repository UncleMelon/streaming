package com.niuwa.streaming;

public class Test {

    public void ClimbStairs(int num) {
        int[] dp = new int[num+1];
        dp[1] = 1;  // 第一层一种走法
        dp[2] = 2;  // 第二层两种走法
        dp[3] = 4;  // 第三层四种走法
        for(int i = 4; i <= num; i++){
            dp[i] = dp[i-1] + dp[i-2] + dp[i-3];
        }
        System.out.println(dp[num]);
    }

    public static void main(String[] args) {
        new Test().ClimbStairs(15);
    }
}
