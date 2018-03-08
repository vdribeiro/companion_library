/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package companion.support.v8.animation;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.animation.AnimationUtils;

/**
 * This class is used to instantiate animator XML files into Animator objects.
 * <p>
 * For performance reasons, inflation relies heavily on pre-processing of
 * XML files that is done at build time. Therefore, it is not currently possible
 * to use this inflater with an XmlPullParser over a plain XML file at runtime;
 * it only works with an XmlPullParser returned from a compiled resource (R.
 * <em>something</em> file.)
 */
@SuppressLint("InlinedApi")
public class AnimatorInflaterCompat {
    private static final int[] AnimatorSet = new int[] {
        /* 0 */ android.R.attr.ordering,
    };
    private static final int AnimatorSet_ordering = 0;

    private static final int[] PropertyAnimator = new int[] {
        /* 0 */ android.R.attr.propertyName,
    };
    private static final int PropertyAnimator_propertyName = 0;

    private static final int[] Animator = new int[] {
        /* 0 */ android.R.attr.interpolator,
        /* 1 */ android.R.attr.duration,
        /* 2 */ android.R.attr.startOffset,
        /* 3 */ android.R.attr.repeatCount,
        /* 4 */ android.R.attr.repeatMode,
        /* 5 */ android.R.attr.valueFrom,
        /* 6 */ android.R.attr.valueTo,
        /* 7 */ android.R.attr.valueType,
    };
    private static final int Animator_interpolator = 0;
    private static final int Animator_duration = 1;
    private static final int Animator_startOffset = 2;
    private static final int Animator_repeatCount = 3;
    private static final int Animator_repeatMode = 4;
    private static final int Animator_valueFrom = 5;
    private static final int Animator_valueTo = 6;
    private static final int Animator_valueType = 7;

    /**
     * These flags are used when parsing AnimatorSet objects
     */
    private static final int TOGETHER = 0;
    //private static final int SEQUENTIALLY = 1;

    /**
     * Enum values used in XML attributes to indicate the value for mValueType
     */
    private static final int VALUE_TYPE_FLOAT       = 0;
    //private static final int VALUE_TYPE_INT         = 1;
    //private static final int VALUE_TYPE_COLOR       = 4;
    //private static final int VALUE_TYPE_CUSTOM      = 5;

    /**
     * Loads an {@link AnimatorCompat} object from a resource
     *
     * @param context Application context used to access resources
     * @param id The resource id of the animation to load
     * @return The animator object reference by the specified id
     * @throws android.content.res.Resources.NotFoundException when the animation cannot be loaded
     */
    @SuppressLint("NewApi")
    public static AnimatorCompat loadAnimator(Context context, int id)
            throws NotFoundException {

        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createAnimatorFromXml(context, parser);
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf =
                    new Resources.NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id), ex);
            throw rnf;
        } catch (IOException ex) {
            Resources.NotFoundException rnf =
                    new Resources.NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id), ex);
            throw rnf;
        } finally {
            if (parser != null) parser.close();
        }
    }

    private static AnimatorCompat createAnimatorFromXml(Context c, XmlPullParser parser)
            throws XmlPullParserException, IOException {

        return createAnimatorFromXml(c, parser, Xml.asAttributeSet(parser), null, 0);
    }

    private static AnimatorCompat createAnimatorFromXml(Context c, XmlPullParser parser,
            AttributeSet attrs, AnimatorSetCompat parent, int sequenceOrdering)
            throws XmlPullParserException, IOException {

        AnimatorCompat anim = null;
        ArrayList<AnimatorCompat> childAnims = null;

        // Make sure we are on a start tag.
        int type;
        int depth = parser.getDepth();

        while (((type=parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
               && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            String  name = parser.getName();

            if (name.equals("objectAnimator")) {
                anim = loadObjectAnimator(c, attrs);
            } else if (name.equals("animator")) {
                anim = loadAnimator(c, attrs, null);
            } else if (name.equals("set")) {
                anim = new AnimatorSetCompat();
                TypedArray a = c.obtainStyledAttributes(attrs,
                        /*com.android.internal.R.styleable.*/AnimatorSet);

                TypedValue orderingValue = new TypedValue();
                a.getValue(/*com.android.internal.R.styleable.*/AnimatorSet_ordering, orderingValue);
                int ordering = orderingValue.type == TypedValue.TYPE_INT_DEC ? orderingValue.data : TOGETHER;

                createAnimatorFromXml(c, parser, attrs, (AnimatorSetCompat) anim,  ordering);
                a.recycle();
            } else {
                throw new RuntimeException("Unknown animator name: " + parser.getName());
            }

            if (parent != null) {
                if (childAnims == null) {
                    childAnims = new ArrayList<AnimatorCompat>();
                }
                childAnims.add(anim);
            }
        }
        if (parent != null && childAnims != null) {
            AnimatorCompat[] animsArray = new AnimatorCompat[childAnims.size()];
            int index = 0;
            for (AnimatorCompat a : childAnims) {
                animsArray[index++] = a;
            }
            if (sequenceOrdering == TOGETHER) {
                parent.playTogether(animsArray);
            } else {
                parent.playSequentially(animsArray);
            }
        }

        return anim;

    }

    private static ObjectAnimatorCompat loadObjectAnimator(Context context, AttributeSet attrs)
            throws NotFoundException {

        ObjectAnimatorCompat anim = new ObjectAnimatorCompat();

        loadAnimator(context, attrs, anim);

        TypedArray a =
                context.obtainStyledAttributes(attrs, /*com.android.internal.R.styleable.*/PropertyAnimator);

        String propertyName = a.getString(/*com.android.internal.R.styleable.*/PropertyAnimator_propertyName);

        anim.setPropertyName(propertyName);

        a.recycle();

        return anim;
    }

    /**
     * Creates a new animation whose parameters come from the specified context and
     * attributes set.
     *
     * @param context the application environment
     * @param attrs the set of attributes holding the animation parameters
     */
    private static ValueAnimatorCompat loadAnimator(Context context, AttributeSet attrs, ValueAnimatorCompat anim)
            throws NotFoundException {

        TypedArray a =
                context.obtainStyledAttributes(attrs, /*com.android.internal.R.styleable.*/Animator);

        long duration = a.getInt(/*com.android.internal.R.styleable.*/Animator_duration, 0);

        long startDelay = a.getInt(/*com.android.internal.R.styleable.*/Animator_startOffset, 0);

        int valueType = a.getInt(/*com.android.internal.R.styleable.*/Animator_valueType,
                VALUE_TYPE_FLOAT);

        if (anim == null) {
            anim = new ValueAnimatorCompat();
        }
        //TypeEvaluator evaluator = null;

        int valueFromIndex = /*com.android.internal.R.styleable.*/Animator_valueFrom;
        int valueToIndex = /*com.android.internal.R.styleable.*/Animator_valueTo;

        boolean getFloats = (valueType == VALUE_TYPE_FLOAT);

        TypedValue tvFrom = a.peekValue(valueFromIndex);
        boolean hasFrom = (tvFrom != null);
        int fromType = hasFrom ? tvFrom.type : 0;
        TypedValue tvTo = a.peekValue(valueToIndex);
        boolean hasTo = (tvTo != null);
        int toType = hasTo ? tvTo.type : 0;

        if ((hasFrom && (fromType >= TypedValue.TYPE_FIRST_COLOR_INT) &&
                (fromType <= TypedValue.TYPE_LAST_COLOR_INT)) ||
            (hasTo && (toType >= TypedValue.TYPE_FIRST_COLOR_INT) &&
                (toType <= TypedValue.TYPE_LAST_COLOR_INT))) {
            // special case for colors: ignore valueType and get ints
            getFloats = false;
            anim.setEvaluator(new ArgbEvaluatorCompat());
        }

        if (getFloats) {
            float valueFrom;
            float valueTo;
            if (hasFrom) {
                if (fromType == TypedValue.TYPE_DIMENSION) {
                    valueFrom = a.getDimension(valueFromIndex, 0f);
                } else {
                    valueFrom = a.getFloat(valueFromIndex, 0f);
                }
                if (hasTo) {
                    if (toType == TypedValue.TYPE_DIMENSION) {
                        valueTo = a.getDimension(valueToIndex, 0f);
                    } else {
                        valueTo = a.getFloat(valueToIndex, 0f);
                    }
                    anim.setFloatValues(valueFrom, valueTo);
                } else {
                    anim.setFloatValues(valueFrom);
                }
            } else {
                if (toType == TypedValue.TYPE_DIMENSION) {
                    valueTo = a.getDimension(valueToIndex, 0f);
                } else {
                    valueTo = a.getFloat(valueToIndex, 0f);
                }
                anim.setFloatValues(valueTo);
            }
        } else {
            int valueFrom;
            int valueTo;
            if (hasFrom) {
                if (fromType == TypedValue.TYPE_DIMENSION) {
                    valueFrom = (int) a.getDimension(valueFromIndex, 0f);
                } else if ((fromType >= TypedValue.TYPE_FIRST_COLOR_INT) &&
                        (fromType <= TypedValue.TYPE_LAST_COLOR_INT)) {
                    valueFrom = a.getColor(valueFromIndex, 0);
                } else {
                    valueFrom = a.getInt(valueFromIndex, 0);
                }
                if (hasTo) {
                    if (toType == TypedValue.TYPE_DIMENSION) {
                        valueTo = (int) a.getDimension(valueToIndex, 0f);
                    } else if ((toType >= TypedValue.TYPE_FIRST_COLOR_INT) &&
                            (toType <= TypedValue.TYPE_LAST_COLOR_INT)) {
                        valueTo = a.getColor(valueToIndex, 0);
                    } else {
                        valueTo = a.getInt(valueToIndex, 0);
                    }
                    anim.setIntValues(valueFrom, valueTo);
                } else {
                    anim.setIntValues(valueFrom);
                }
            } else {
                if (hasTo) {
                    if (toType == TypedValue.TYPE_DIMENSION) {
                        valueTo = (int) a.getDimension(valueToIndex, 0f);
                    } else if ((toType >= TypedValue.TYPE_FIRST_COLOR_INT) &&
                        (toType <= TypedValue.TYPE_LAST_COLOR_INT)) {
                        valueTo = a.getColor(valueToIndex, 0);
                    } else {
                        valueTo = a.getInt(valueToIndex, 0);
                    }
                    anim.setIntValues(valueTo);
                }
            }
        }

        anim.setDuration(duration);
        anim.setStartDelay(startDelay);

        if (a.hasValue(/*com.android.internal.R.styleable.*/Animator_repeatCount)) {
            anim.setRepeatCount(
                    a.getInt(/*com.android.internal.R.styleable.*/Animator_repeatCount, 0));
        }
        if (a.hasValue(/*com.android.internal.R.styleable.*/Animator_repeatMode)) {
            anim.setRepeatMode(
                    a.getInt(/*com.android.internal.R.styleable.*/Animator_repeatMode,
                            ValueAnimatorCompat.RESTART));
        }
        //if (evaluator != null) {
        //    anim.setEvaluator(evaluator);
        //}

        final int resID =
                a.getResourceId(/*com.android.internal.R.styleable.*/Animator_interpolator, 0);
        if (resID > 0) {
            anim.setInterpolator(AnimationUtils.loadInterpolator(context, resID));
        }
        a.recycle();

        return anim;
    }
}