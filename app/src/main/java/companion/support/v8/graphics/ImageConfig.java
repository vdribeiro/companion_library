package companion.support.v8.graphics;

/**
 * Image configurations.
 *
 * @author Vitor Ribeiro
 */
class ImageConfig {

    private int srcWidth = 0;
    private int srcHeigth = 0;

    int dstWidth = 0;
    int dstHeigth = 0;
    int inSampleSize = 1;

    ImageConfig(int srcWidth, int srcHeigth, int maxSize) {
        this.srcWidth = srcWidth;
        this.srcHeigth = srcHeigth;

        calculateConfigs(maxSize);
    }

    private void calculateInSampleSize(int maxSize) {
        int halfWidth = srcWidth / 2;
        int halfHeight = srcHeigth / 2;

        inSampleSize = 1;
        while ((halfWidth / inSampleSize) >= maxSize &&
                (halfHeight / inSampleSize) >= maxSize) {
            inSampleSize *= 2;
        }
    }

    private void calculateScaledSizes(int maxSize) {
        float scale;
        if (srcWidth > srcHeigth) {
            scale = maxSize / srcWidth;
        } else {
            scale = maxSize / srcHeigth;
        }

        dstWidth = Math.round(srcWidth * scale);
        dstHeigth = Math.round(srcHeigth * scale);
    }

    /**
     * Get scaled sizes limited by the defined maximum and
     * calculate the largest inSampleSize value that is a power of 2 and keeps both
     * height and width larger than the defined maximum.
     * @param maxSize maximum image size in pixels.
     */
    private void calculateConfigs(int maxSize) {
        if (srcWidth == 0 || srcHeigth == 0) {
            dstWidth = maxSize;
            dstHeigth = maxSize;
            return;
        }

        if (srcWidth <= maxSize && srcHeigth <= maxSize) {
            dstWidth = srcWidth;
            dstHeigth = srcHeigth;
            return;
        }

        calculateInSampleSize(maxSize);
        calculateScaledSizes(maxSize);
    }

    int getInDensity() {
        if (srcWidth > srcHeigth) {
            return srcWidth;
        } else {
            return srcHeigth;
        }
    }

    int getInTargetDensity() {
        if (dstWidth > dstHeigth) {
            return dstWidth * inSampleSize;
        } else {
            return dstHeigth * inSampleSize;
        }
    }
}