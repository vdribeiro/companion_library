package companion.support.v8.view;

import java.lang.ref.WeakReference;

import android.view.View;
import android.view.animation.Interpolator;

import companion.support.v8.animation.AnimatorCompat.AnimatorListener;

class ViewPropertyAnimatorICS extends ViewPropertyAnimatorCompat {
    /**
     * A value to be returned when the WeakReference holding the native implementation
     * returns <code>null</code>
     */
    private final static long RETURN_WHEN_NULL = -1L;

    /**
     * A WeakReference holding the native implementation of ViewPropertyAnimator
     */
    private final WeakReference<android.view.ViewPropertyAnimator> mNative;

    ViewPropertyAnimatorICS(View view) {
        mNative = new WeakReference<android.view.ViewPropertyAnimator>(view.animate());
    }

    @Override
    public ViewPropertyAnimatorCompat setDuration(long duration) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.setDuration(duration);
        }
        return this;
    }

    @Override
    public long getDuration() {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            return n.getDuration();
        }
        return RETURN_WHEN_NULL;
    }

    @Override
    public ViewPropertyAnimatorCompat setStartDelay(long startDelay) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.setStartDelay(startDelay);
        }
        return this;
    }

    @Override
    public long getStartDelay() {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            return n.getStartDelay();
        }
        return RETURN_WHEN_NULL;
    }

    @Override
    public ViewPropertyAnimatorCompat setInterpolator(Interpolator interpolator) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.setInterpolator(interpolator);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat setListener(final AnimatorListener listener) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            if (listener == null) {
                n.setListener(null);
            } else {
                n.setListener(new android.animation.Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation) {
                        listener.onAnimationStart(null);
                    }

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation) {
                        listener.onAnimationRepeat(null);
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        listener.onAnimationEnd(null);
                    }

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation) {
                        listener.onAnimationCancel(null);
                    }
                });
            }
        }
        return this;
    }

    @Override
    public void start() {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.start();
        }
    }

    @Override
    public void cancel() {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.cancel();
        }
    }

    @Override
    public ViewPropertyAnimatorCompat x(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.x(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat xBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.xBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat y(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.y(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat yBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.yBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat rotation(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.rotation(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat rotationBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.rotationBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat rotationX(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.rotationX(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat rotationXBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.rotationXBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat rotationY(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.rotationY(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat rotationYBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.rotationYBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat translationX(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.translationX(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat translationXBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.translationXBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat translationY(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.translationY(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat translationYBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.translationYBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat scaleX(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.scaleX(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat scaleXBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.scaleXBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat scaleY(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.scaleY(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat scaleYBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.scaleYBy(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat alpha(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.alpha(value);
        }
        return this;
    }

    @Override
    public ViewPropertyAnimatorCompat alphaBy(float value) {
        android.view.ViewPropertyAnimator n = mNative.get();
        if (n != null) {
            n.alphaBy(value);
        }
        return this;
    }
}
