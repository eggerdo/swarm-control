package org.dobots.utility;

public interface AccelerometerListener
{
    public void onAccelerationChanged( float x, float y, float z, boolean tx );
//    public void onShake( float force );
}

