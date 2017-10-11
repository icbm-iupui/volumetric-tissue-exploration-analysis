/*
 * Copyright (C) 2017 SciJava
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.visualization.clearvolume;

import java.nio.ByteBuffer;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.cleargl.ClearGLVolumeRenderer;
import clearvolume.renderer.cleargl.utils.ScreenToEyeRay.EyeRay;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.renderer.listeners.EyeRayListener;
import clearvolume.transferf.TransferFunctions;
import coremem.enums.NativeTypeEnum;
import com.jogamp.newt.event.MouseEvent;

/**
 *
 * @author sethwinfree
 */
public class ClearVolumeRenderer {

    public ClearVolumeRenderer() throws InterruptedException {

        final ClearVolumeRendererInterface lClearVolumeRenderer
                = ClearVolumeRendererFactory.newBestRenderer("ClearVolumeTest",
                        512,
                        512,
                        NativeTypeEnum.UnsignedByte,
                        512,
                        512,
                        1,
                        false);
        lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
        lClearVolumeRenderer.setVisible(true);

        final int lResolutionX = 512;
        final int lResolutionY = lResolutionX;
        final int lResolutionZ = lResolutionX;

        final byte[] lVolumeDataArray
                = new byte[lResolutionX * lResolutionY
                * lResolutionZ];

        for (int z = 0; z < lResolutionZ; z++) {
            for (int y = 0; y < lResolutionY; y++) {
                for (int x = 0; x < lResolutionX; x++) {
                    final int lIndex = x + lResolutionX * y
                            + lResolutionX * lResolutionY * z;
                    int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
                    if (lCharValue < 12) {
                        lCharValue = 0;
                    }
                    lVolumeDataArray[lIndex] = (byte) lCharValue;
                }
            }
        }

        lClearVolumeRenderer.setVolumeDataBuffer(0,
                ByteBuffer.wrap(lVolumeDataArray),
                lResolutionX,
                lResolutionY,
                lResolutionZ);

        lClearVolumeRenderer.addEyeRayListener(new EyeRayListener() {

            @Override
            public boolean notifyEyeRay(ClearGLVolumeRenderer pRenderer,
                    MouseEvent pMouseEvent,
                    EyeRay pEyeRay) {
                if (pMouseEvent.getButton() != 2
                        && pMouseEvent.getEventType() != MouseEvent.EVENT_MOUSE_CLICKED) {
                    return false;
                }

                final int lX = pMouseEvent.getX();
                final int lY = pMouseEvent.getY();

                System.out.format("%d %d \n", lX, lY);

                System.out.println(pMouseEvent);
                System.out.println(pEyeRay);

                float x = pEyeRay.org[0];
                float y = pEyeRay.org[1];
                float z = pEyeRay.org[2];

                boolean lOnceIn = false;
                final float lStepSize = 0.001f;
                for (float i = 0; i < 100000 * lStepSize; i += lStepSize) {
                    x += lStepSize * pEyeRay.dir[0];
                    y += lStepSize * pEyeRay.dir[1];
                    z += lStepSize * pEyeRay.dir[2];

                    final int ix = (int) (lResolutionX * x);
                    final int iy = (int) (lResolutionY * y);
                    final int iz = (int) (lResolutionZ * z);

                    if (ix < 0 || ix >= lResolutionX
                            || iy < 0
                            || iy >= lResolutionY
                            || iz < 0
                            || iz >= lResolutionZ) {
                        if (lOnceIn) {
                            break;
                        } else {
                            continue;
                        }
                    }

                    lOnceIn = true;
                    final int lIndex = ix + lResolutionX * iy
                            + lResolutionX * lResolutionY * iz;

                    lVolumeDataArray[lIndex] = (byte) 200;

                }

                lClearVolumeRenderer.setVolumeDataBuffer(0,
                        ByteBuffer.wrap(lVolumeDataArray),
                        lResolutionX,
                        lResolutionY,
                        lResolutionZ);
                return false;

            }

        });
        
}
    
}