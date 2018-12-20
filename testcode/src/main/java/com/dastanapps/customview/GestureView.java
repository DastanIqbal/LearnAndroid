package com.dastanapps.customview;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dastanapps.gesture.MoveGestureDetector;
import com.dastanapps.testcode.R;

public abstract class GestureView extends FrameLayout {
    private boolean enableBorder = true;
    private float durationStart;
    private float durationEnd;
    private int touchSlop;
    float downX = 0f, downY = 0f;
    private PointF xy;
    private ScaleGestureDetector mScaleDetector;
    private MoveGestureDetector mMoveDetector;

    private float mScaleFactor = 1.f;
    private boolean islock = false;
    private PointF translationPoint = new PointF();
    private boolean toggleTouch = true;
    protected double mAspectRatio = 1.0;
    private IGestureOperation iStickerOperation;
    private float mPosX;
    private float mPosY;

    private float mLastGestureX;
    private float mLastGestureY;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            postInvalidate();
            requestLayout();
            return true;
        }
    }

    private float mFocusX = 0.f;
    private float mFocusY = 0.f;

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {

            PointF d = detector.getFocusDelta();
            mFocusX += d.x;
            mFocusY += d.y;
            return true;
        }
    }

    public static final String TAG = GestureView.class.getSimpleName();
    private BorderView iv_border;
    private ImageView iv_scale;
    private ImageView iv_delete;
    private ImageView iv_flip;

    // For scaling
    private float this_orgX = -1, this_orgY = -1;
    private float scale_orgX = -1, scale_orgY = -1;
    private double scale_orgWidth = -1, scale_orgHeight = -1;
    // For rotating
    private float rotate_orgX = -1, rotate_orgY = -1, rotate_newX = -1, rotate_newY = -1;
    // For moving
    private float move_orgX = -1, move_orgY = -1;

    private double centerX, centerY;

    protected final static int BUTTON_SIZE_DP = 30;
    protected final static int SELF_SIZE_DP = 70;

    public GestureView(Context context) {
        super(context);
        init(context);
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        mMoveDetector = new MoveGestureDetector(context, new MoveListener());
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.iv_border = new BorderView(context);
        this.iv_scale = new ImageView(context);
        this.iv_delete = new ImageView(context);
        this.iv_flip = new ImageView(context);

        this.iv_scale.setImageResource(R.mipmap.ic_image);
        this.iv_delete.setImageResource(R.drawable.ic_stat_nightfilter);
        this.iv_flip.setImageResource(R.drawable.ic_stat_nightfilter);

        this.setTag("DraggableViewGroup");
        this.iv_border.setTag("iv_border");
        this.iv_scale.setTag("iv_scale");
        this.iv_delete.setTag("iv_delete");
        this.iv_flip.setTag("iv_flip");

        int margin = convertDpToPixel(BUTTON_SIZE_DP, getContext()) / 2;
        int size = convertDpToPixel(SELF_SIZE_DP, getContext());
        int padding = 8;//convertDpToPixel(4, getContext());

        LayoutParams this_params =
                new LayoutParams(size, size);
        this_params.gravity = Gravity.CENTER;

        LayoutParams iv_main_params =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iv_main_params.setMargins(margin, margin, margin, margin);

        LayoutParams iv_border_params =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iv_border_params.setMargins(margin, margin, margin, margin);

        LayoutParams iv_scale_params =
                new LayoutParams(convertDpToPixel(BUTTON_SIZE_DP, getContext()), convertDpToPixel(BUTTON_SIZE_DP, getContext()));
        iv_scale_params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        LayoutParams iv_delete_params =
                new LayoutParams(convertDpToPixel(BUTTON_SIZE_DP, getContext()), convertDpToPixel(BUTTON_SIZE_DP, getContext()));
        iv_delete_params.gravity = Gravity.TOP | Gravity.LEFT;

        LayoutParams iv_flip_params =
                new LayoutParams(convertDpToPixel(BUTTON_SIZE_DP, getContext()), convertDpToPixel(BUTTON_SIZE_DP, getContext()));
        iv_flip_params.gravity = Gravity.TOP | Gravity.RIGHT;

        this.iv_scale.setPadding(padding, padding, padding, padding);
        this.iv_delete.setPadding(padding, padding, padding, padding);

        this.setLayoutParams(this_params);
        this.addView(getMainView(), iv_main_params);
        this.addView(iv_border, iv_border_params);
        this.addView(iv_scale, iv_scale_params);
        this.addView(iv_delete, iv_delete_params);
        this.addView(iv_flip, iv_flip_params);
        this.setOnTouchListener(mTouchListener);
        this.iv_scale.setOnTouchListener(mTouchListener);
        this.iv_delete.setOnClickListener(view -> {
            if (GestureView.this.getParent() != null) {
                ViewGroup myCanvas = ((ViewGroup) GestureView.this.getParent());
                myCanvas.removeView(GestureView.this);
                if (iStickerOperation != null) {
                    iStickerOperation.onDelete((String) getTag(R.id.always));
                }
            }
        });
        this.iv_flip.setOnClickListener(view -> {

            View mainView = getMainView();
            mainView.setRotationY(mainView.getRotationY() == -180f ? 0f : -180f);
            mainView.invalidate();
            requestLayout();
//                if (GestureView.this.getParent() != null) {
//                    if (iStickerOperation != null) {
//                        iStickerOperation.onEdit((String) getTag(R.id.sticker_tag));
//                    }
//                }
        });


        this.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateTranslation());
    }

    public boolean isFlip() {
        return getMainView().getRotationY() == -180f;
    }

    protected abstract View getMainView();

    public double getAspectRatio() {
        return mAspectRatio;
    }

    boolean isSingle = false;

    PointF getRawPoint(MotionEvent ev, int index) {
        PointF point = new PointF();
        final int location[] = {0, 0};
        getLocationOnScreen(location);

        float x = ev.getX(index);
        float y = ev.getY(index);

        double angle = Math.toDegrees(Math.atan2(y, x));
        angle += getRotation();

        final float length = PointF.length(x, y);

        x = (float) (length * Math.cos(Math.toRadians(angle))) + location[0];
        y = (float) (length * Math.sin(Math.toRadians(angle))) + location[1];

        point.set(x, y);
        return point;
    }

    protected float calculateRotation(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        PointF firstPointer = getRawPoint(event, 0);
        PointF secondPointer = getRawPoint(event, 1);
        return calculateRotation(firstPointer.x, firstPointer.y, secondPointer.x, secondPointer.y);
    }

    protected float calculateRotation(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        double radians = Math.atan2(y, x);
        Log.v("kkl2", x1 + "x" + y1 + ", " + x2 + "x" + y2);
        return (float) Math.toDegrees(radians);
    }

    protected float calculateDistance(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        PointF firstPointer = getRawPoint(event, 0);
        PointF secondPointer = getRawPoint(event, 1);
        return calculateDistance(firstPointer.x, firstPointer.y, secondPointer.x, secondPointer.y);
    }

    protected float calculateDistance(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;

        return (float) Math.sqrt(x * x + y * y);
    }

    public void toggleTouchEvents(boolean enable) {
        this.toggleTouch = enable;
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {

        final int ROTATION_THRESHOLD = 10;
        boolean isTwoFinger;
        boolean hasResetAfterTwoFinger;
        float oldRotation;
        float oldDistance;
        float currentViewRotation;
        Point currentViewSize = new Point();

        private void save(MotionEvent event) {
            oldRotation = calculateRotation(event);
            currentViewRotation = getRotation();
            oldDistance = calculateDistance(event);
            currentViewSize.set(getMainView().getWidth(), getMainView().getHeight());
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (!toggleTouch) {
                return false;
            } else if (enableBorder) {
                if (view.getTag() != null && view.getTag().equals("DraggableViewGroup")) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            save(event);
                            if (!isTwoFinger) {
                                move_orgX = event.getRawX();
                                move_orgY = event.getRawY();
                                scale_orgX = event.getRawX();
                                scale_orgY = event.getRawY();
                                hasResetAfterTwoFinger = true;
                            }
                            break;
                        case MotionEvent.ACTION_POINTER_DOWN:
                            isTwoFinger = true;
                            hasResetAfterTwoFinger = false;

                            save(event);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (isTwoFinger) {
                                double scale = calculateDistance(event) / oldDistance;

                                double finalWidth = currentViewSize.x * scale;
                                double finalHeight = finalWidth * mAspectRatio;
                                GestureView.this.getLayoutParams().width = (int) finalWidth + getButtonSize();
                                GestureView.this.getLayoutParams().height = (int) finalHeight + getButtonSize();
                                onScaling(scale > 1);

                                //rotate
                                double angle = currentViewRotation + calculateRotation(event) - oldRotation;
                                if (angle >= -ROTATION_THRESHOLD && angle <= ROTATION_THRESHOLD) {
                                    angle = 0;
                                }
                               // setRotation((float) angle);
                                onRotating();

                                postInvalidate();
                                requestLayout();
                                break;
                            } else if (hasResetAfterTwoFinger) {
                                setTranslation(event);
                                scale_orgWidth = GestureView.this.getLayoutParams().width;
                                scale_orgHeight = GestureView.this.getLayoutParams().height;
                            }
                            break;
                        case MotionEvent.ACTION_POINTER_UP:
                            Log.v(TAG, "sticker view action pointer up");
                            isTwoFinger = false;
                            break;
                        case MotionEvent.ACTION_UP:
                            Rect bounds = new Rect();
                            getGlobalVisibleRect(bounds);
                            updateXY();
                            break;
                    }
                } else if (view.getTag() != null && view.getTag().equals("iv_scale")) {
                    mScaleDetector.onTouchEvent(event);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            this_orgX = GestureView.this.getX();
                            this_orgY = GestureView.this.getY();

                            scale_orgX = event.getRawX();
                            scale_orgY = event.getRawY();
                            scale_orgWidth = GestureView.this.getLayoutParams().width;
                            scale_orgHeight = GestureView.this.getLayoutParams().height;

                            rotate_orgX = event.getRawX();
                            rotate_orgY = event.getRawY();

                            centerX = GestureView.this.getX() +
                                    ((View) GestureView.this.getParent()).getX() +
                                    (float) GestureView.this.getWidth() / 2;


                            calculateCenterY();

                            break;
                        case MotionEvent.ACTION_MOVE:
                            rotate_newX = event.getRawX();
                            rotate_newY = event.getRawY();

                            double angle_diff = Math.abs(
                                    Math.atan2(event.getRawY() - scale_orgY, event.getRawX() - scale_orgX)
                                            - Math.atan2(scale_orgY - centerY, scale_orgX - centerX)) * 180 / Math.PI;

                            double length1 = getLength(centerX, centerY, scale_orgX, scale_orgY);
                            double length2 = getLength(centerX, centerY, event.getRawX(), event.getRawY());

                            int size = convertDpToPixel(SELF_SIZE_DP, getContext());
                            if (length2 > length1
                                    && (angle_diff < 25 || Math.abs(angle_diff - 180) < 25)
                                    ) {
                                //scale up
                                double offsetX = Math.abs(event.getRawX() - scale_orgX);
                                double offsetY = Math.abs(event.getRawY() - scale_orgY);
                                double offset = Math.max(offsetX, offsetY);
                                double offsetFinalX = Math.round(offset);

                                double finalX = getMainView().getWidth() + offsetFinalX;
                                double finalY = finalX * mAspectRatio;
                                GestureView.this.getLayoutParams().width = (int) (finalX + getButtonSize());
                                GestureView.this.getLayoutParams().height = (int) (finalY + getButtonSize());
                                onScaling(true);
                                //DraggableViewGroup.this.setX((float) (getX() - offset / 2));
                                //DraggableViewGroup.this.setY((float) (getY() - offset / 2));
                            } else if (length2 < length1
                                    && (angle_diff < 25 || Math.abs(angle_diff - 180) < 25)
                                    && GestureView.this.getLayoutParams().width > size / 2
                                    && GestureView.this.getLayoutParams().height > size / 2) {
                                //scale down
                                double offsetX = Math.abs(event.getRawX() - scale_orgX);
                                double offsetY = Math.abs(event.getRawY() - scale_orgY);
                                double offset = Math.max(offsetX, offsetY);
                                offset = Math.round(offset);
                                double offsetFinalX = Math.round(offset);

                                double finalX = getMainView().getWidth() - offsetFinalX;
                                double finalY = finalX * mAspectRatio;
                                GestureView.this.getLayoutParams().width = (int) (finalX + getButtonSize());
                                GestureView.this.getLayoutParams().height = (int) (finalY + getButtonSize());
                                onScaling(false);
                            }

                            //rotate

                            double angle = Math.atan2(event.getRawY() - centerY, event.getRawX() - centerX) * 180 / Math.PI;
                            float targetAngle = (float) angle - 45;
                            if (targetAngle >= -ROTATION_THRESHOLD && targetAngle <= ROTATION_THRESHOLD) {
                                targetAngle = 0;
                            }
                           // setRotation(targetAngle);

                            onRotating();

                            rotate_orgX = rotate_newX;
                            rotate_orgY = rotate_newY;

                            scale_orgX = event.getRawX();
                            scale_orgY = event.getRawY();

                            postInvalidate();
                            requestLayout();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                    }
                }
            } else {
                //convert onTouch to onClick
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(event.getX() - downX) < touchSlop
                                && Math.abs(event.getY() - downY) < touchSlop) {
                            if (iStickerOperation != null && !islock) {
                                iStickerOperation.onSelect(GestureView.this, (String) getTag(R.id.always));
                            }
                        }
                        break;
                }
            }

            return true;
        }
    };

    protected int getButtonSize() {
        return convertDpToPixel(BUTTON_SIZE_DP, getContext());
    }

    private void calculateCenterY() {
        //double statusBarHeight = Math.ceil(25 * getContext().getResources().getDisplayMetrics().density);
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        double statusBarHeight = result;
        centerY = GestureView.this.getY() +
                ((View) GestureView.this.getParent()).getY() +
                statusBarHeight +
                (float) GestureView.this.getHeight() / 2;
    }

    private void setTranslation(MotionEvent event) {
        float offsetX = event.getRawX() - move_orgX;
        float offsetY = event.getRawY() - move_orgY;
        translationPoint.x += offsetX;
        translationPoint.y += offsetY;
        float targetX = translationPoint.x;
        float targetY = translationPoint.y;
        float targetCenterX = getCenterX(targetX);
        float targetCenterY = getCenterY(targetY);

        final float RANGE = convertDpToPixel(10, getContext());
        PointF parentCenterPoint = getParentCenterPoint();
        final float MIN_X = parentCenterPoint.x - RANGE;
        final float MAX_X = parentCenterPoint.x + RANGE;
        final float MIN_Y = parentCenterPoint.y - RANGE;
        final float MAX_Y = parentCenterPoint.y + RANGE;

        boolean shouldSnapX = targetCenterX >= MIN_X && targetCenterX <= MAX_X;
        boolean shouldSnapY = targetCenterY >= MIN_Y && targetCenterY <= MAX_Y;
        boolean shouldSnapAll = shouldSnapX && shouldSnapY;
        if (shouldSnapAll) {
            targetX = getX(parentCenterPoint.x);
            targetY = getY(parentCenterPoint.y);
        } else if (shouldSnapX) {
            targetX = getX(parentCenterPoint.x);
        } else if (shouldSnapY) {
            targetY = getY(parentCenterPoint.y);
        } else {
        }
        setX(targetX);
        setY(targetY);

      //  move_orgX = event.getRawX();
     //   move_orgY = event.getRawY();
    }

    public void setTranslation(float x, float y) {
        setX(x);
        setY(y);
        updateTranslation();
    }


    public void updateTranslation() {
        translationPoint.set(getX(), getY());
    }


    private PointF getParentCenterPoint() {
        View parent = (View) getParent();
        float parentCenterX = parent.getWidth() / 2f;
        float parentCenterY = parent.getHeight() / 2f;
        return new PointF(parentCenterX, parentCenterY);
    }

    private float getX(float centerX) {
        return centerX - getWidth() / 2f;
    }

    private float getY(float centerY) {
        return centerY - getHeight() / 2f;
    }


    private float getCenterX(float targetX) {
        return targetX + getWidth() / 2f;
    }

    private float getCenterY(float targetY) {
        return targetY + getHeight() / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        canvas.translate(mPosX, mPosY);

        if (mScaleDetector.isInProgress()) {
            canvas.scale(mScaleFactor, mScaleFactor, mScaleDetector.getFocusX(), mScaleDetector.getFocusY());
        } else {
            canvas.scale(mScaleFactor, mScaleFactor, mLastGestureX, mLastGestureY);
        }
        super.onDraw(canvas);
        canvas.restore();
    }

    private double getLength(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }

    private float[] getRelativePos(float absX, float absY) {
        float[] pos = new float[]{
                absX - ((View) this.getParent()).getX(),
                absY - ((View) this.getParent()).getY()
        };
        return pos;
    }

    public void setControlItemsHidden(boolean isHidden) {
        if (isHidden) {
            iv_border.setVisibility(View.INVISIBLE);
            iv_scale.setVisibility(View.INVISIBLE);
            iv_delete.setVisibility(View.INVISIBLE);
            iv_flip.setVisibility(View.INVISIBLE);
        } else {
            iv_border.setVisibility(View.VISIBLE);
            iv_scale.setVisibility(View.VISIBLE);
            iv_delete.setVisibility(View.VISIBLE);
            iv_flip.setVisibility(View.INVISIBLE);
        }
    }

    protected View getImageViewFlip() {
        return iv_flip;
    }

    protected void onScaling(boolean scaleUp) {
    }

    protected void onRotating() {
    }

    public void lock(boolean b) {
        this.islock = b;
    }

    public void select(boolean b) {
        enableBorder = b;
        setControlItemsHidden(!b);
        invalidate();
    }

    public void setVisible(final boolean b) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (b) {
                setVisibility(VISIBLE);
            } else {
                setVisibility(INVISIBLE);
            }
        });
    }

    public void updateXY() {
        xy = new PointF((getX() + getMeasuredWidth() / 2), (getY() + getMeasuredHeight() / 2));
        int[] pos = new int[2];
        getLocationOnScreen(pos);
    }

    public PointF getXy() {
        return xy;
    }

    private class BorderView extends View {

        private Rect border = new Rect();
        private Paint borderPaint = new Paint();

        public BorderView(Context context) {
            super(context);
        }

        public BorderView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public BorderView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // Draw sticker border

            LayoutParams params = (LayoutParams) this.getLayoutParams();
            border.left = (int) this.getLeft() - params.leftMargin;
            border.top = (int) this.getTop() - params.topMargin;
            border.right = (int) this.getRight() - params.rightMargin;
            border.bottom = (int) this.getBottom() - params.bottomMargin;
            borderPaint.setStrokeWidth(6);
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(border, borderPaint);

            updateXY();
        }
    }

    protected static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    public boolean isSelected() {
        return enableBorder;
    }
}
