package com.motus.motusupper;

public class KalmanFilter
{
	public float Q_angle;
	public float Q_bias;
	public float R_measure;
	public float angle;
	public float bias;
	public float[][]  P;

	public void init()
	{
		Q_angle = 0.001f;
		Q_bias = 0.003f;
		R_measure = 0.03f;
		
		angle = 0.0f;
		bias = 0.0f;
		P = new float[2][2];
		P[0][0] = 0.0f;
		P[0][1] = 0.0f;
		P[1][0] = 0.0f;
		P[1][1] = 0.0f;
	}

	public float GetAngle(float newAngle, float newRate, float dt)
	{

		 /* Step 1 */
		float rate,S,y;
		float[]  K;
        K = new float[2];
		rate = newRate - bias;
        angle += dt * rate;
        /* Step 2 */
        P[0][0] += dt * (dt*P[1][1] - P[0][1] - P[1][0] + Q_angle);
        P[0][1] -= dt * P[1][1];
        P[1][0] -= dt * P[1][1];
        P[1][1] += Q_bias * dt;

        /* Step 4 */
        S = P[0][0] + R_measure;
        /* Step 5 */
        K[0] = P[0][0] / S;
        K[1] = P[1][0] / S;

        // Calculate angle and bias - Update estimate with measurement zk (newAngle)
        /* Step 3 */
        y = newAngle - angle;
        /* Step 6 */
        angle += K[0] * y;
        bias += K[1] * y;

        // Calculate estimation error covariance - Update the error covariance
        /* Step 7 */
        P[0][0] -= K[0] * P[0][0];
        P[0][1] -= K[0] * P[0][1];
        P[1][0] -= K[1] * P[0][0];
        P[1][1] -= K[1] * P[0][1];
		
        return angle;
	}

}
