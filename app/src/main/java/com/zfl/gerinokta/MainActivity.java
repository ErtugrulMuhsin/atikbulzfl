package com.zfl.gerinokta;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 42;
    private static final GeoPoint ZONGULDAK_CENTER = new GeoPoint(41.4535, 31.7894);
    private static final String PREFS = "atikbul_settings";
    private static final String KEY_THEME = "theme";
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private static final String GITHUB_URL = "https://github.com/ErtugrulMuhsin/atikbulzfl";
    private static final String FEEDBACK_EMAIL = "atikbulzfl@tutamail.com";
    private static final int BUTTON_RADIUS_DP = 18;
    private static final int CHIP_HEIGHT_DP = 42;
    private static final XYTileSource DARK_TILE_SOURCE = new XYTileSource(
            "CartoDarkMatter",
            0,
            20,
            256,
            ".png",
            new String[]{
                    "https://a.basemaps.cartocdn.com/dark_all/",
                    "https://b.basemaps.cartocdn.com/dark_all/",
                    "https://c.basemaps.cartocdn.com/dark_all/",
                    "https://d.basemaps.cartocdn.com/dark_all/"
            });

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private WasteCategory selectedCategory;
    private Location userLocation;
    private final List<Marker> activeMarkers = new ArrayList<>();
    private final List<Button> filterButtons = new ArrayList<>();
    private final List<WasteCategory> filterButtonCategories = new ArrayList<>();
    private TextView statusText;
    private LinearLayout root;
    private Palette palette;
    private boolean settingsVisible;

    enum WasteCategory {
        BATTERY("Atık pil", "Pil", 0xFF795548, 0.00000, 0.00000),
        PAPER("Kağıt", "Kağıt", 0xFF2E7D32, 0.00008, -0.00008),
        PLASTIC("Plastik", "Plastik", 0xFF1565C0, -0.00008, 0.00008),
        GLASS("Cam", "Cam", 0xFF00897B, 0.00010, 0.00010),
        OIL("Yağ", "Yağ", 0xFFE07A00, -0.00010, -0.00010);

        final String label;
        final String shortLabel;
        final int color;
        final double latOffset;
        final double lonOffset;

        WasteCategory(String label, String shortLabel, int color, double latOffset, double lonOffset) {
            this.label = label;
            this.shortLabel = shortLabel;
            this.color = color;
            this.latOffset = latOffset;
            this.lonOffset = lonOffset;
        }
    }

    static class RecyclingPoint {
        final String name;
        final String address;
        final String phone;
        final String manager;
        final double latitude;
        final double longitude;
        final boolean approximate;
        final EnumSet<WasteCategory> accepts;

        RecyclingPoint(String name, String address, String phone, String manager, double latitude, double longitude,
                       boolean approximate, EnumSet<WasteCategory> accepts) {
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.manager = manager;
            this.latitude = latitude;
            this.longitude = longitude;
            this.approximate = approximate;
            this.accepts = accepts;
        }
    }

    static class Palette {
        final int background;
        final int surface;
        final int surfaceHigh;
        final int primary;
        final int onPrimary;
        final int onSurface;
        final int onSurfaceVariant;
        final int outline;
        final int outlineSoft;
        final int statusBar;
        final int navBar;
        final int mapScrim;
        final boolean dark;

        Palette(int background, int surface, int surfaceHigh, int primary, int onPrimary,
                int onSurface, int onSurfaceVariant, int outline, int outlineSoft, int statusBar,
                int navBar, int mapScrim, boolean dark) {
            this.background = background;
            this.surface = surface;
            this.surfaceHigh = surfaceHigh;
            this.primary = primary;
            this.onPrimary = onPrimary;
            this.onSurface = onSurface;
            this.onSurfaceVariant = onSurfaceVariant;
            this.outline = outline;
            this.outlineSoft = outlineSoft;
            this.statusBar = statusBar;
            this.navBar = navBar;
            this.mapScrim = mapScrim;
            this.dark = dark;
        }
    }

    private final List<RecyclingPoint> points = Arrays.asList(
            new RecyclingPoint("Üzülmez İlkokulu", "Çınartepe Mah. Aydıntepe Sok. No:34, Merkez/Zonguldak",
                    "0372 268 18 41", "Ayşen YÜCESAN", 41.454578, 31.820539, false,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER, WasteCategory.PLASTIC)),
            new RecyclingPoint("Murat Kayhan İlkokulu/Ortaokulu", "Eceler Köyü Çay Mevkii, Merkez/Zonguldak",
                    "0372 212 51 04", "Onur IŞIK", 41.441600, 31.984500, true,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER)),
            new RecyclingPoint("İbrahim Fikri Anıl Ortaokulu", "Bahçelievler Mah. Gül Sok. No:22, Merkez/Zonguldak",
                    "0372 281 00 38", "Yakup BALÇIN", 41.443900, 31.812700, true,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER)),
            new RecyclingPoint("TOBB Uzunmehmet İlk/Ortaokulu", "Karaelmas Mah. Bilim Sok. No:1, Merkez/Zonguldak",
                    "0372 252 19 61", "Osman YILMAZ", 41.451069, 31.801153, false,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER)),
            new RecyclingPoint("Karaelmas İlkokulu", "Karaelmas Mah. Elvan Kazancı Sok. No:39, Merkez/Zonguldak",
                    "0372 251 21 90", "Servet ÇÜMEN", 41.448723, 31.795965, false,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER, WasteCategory.PLASTIC, WasteCategory.GLASS, WasteCategory.OIL)),
            new RecyclingPoint("Beycuma Şehit Polis Çağdaş Arslan ÇPAL", "Merkez Mah. İstiklal Cad. No:7, Beycuma/Zonguldak",
                    "0372 211 24 41", "İlknur BİRİNCİ", 41.330600, 31.962900, true,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER)),
            new RecyclingPoint("Mehmet Çelikel Lisesi", "Terakki Mah. Lise Sok. No:4, Merkez/Zonguldak",
                    "0372 257 75 75", "Aziz ESE", 41.450420, 31.779701, false,
                    EnumSet.of(WasteCategory.BATTERY)),
            new RecyclingPoint("Mimar Sinan Ortaokulu", "Birlik Mah. Yıldırım Sok. No:5, Merkez/Zonguldak",
                    "0372 253 67 25", "Cenk Cem BAYRAM", 41.455000, 31.787600, true,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER)),
            new RecyclingPoint("Şehit İlhan Varank KAİHL", "Karaelmas Mah. Şehit Bülent Şanal Sok. No:7, Merkez/Zonguldak",
                    "0372 201 09 27", "Emine YILMAZ", 41.450900, 31.798600, true,
                    EnumSet.noneOf(WasteCategory.class)),
            new RecyclingPoint("Prof. Dr. Şaban Teoman Duralı BİLSEM", "Terakki Mah. Gündoğdu Sok. No:4/A, Merkez/Zonguldak",
                    "0372 257 14 06", "Mustafa GÖKGÖZ", 41.450200, 31.780900, true,
                    EnumSet.of(WasteCategory.BATTERY, WasteCategory.PAPER))
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        palette = loadPalette();
        applyWindowColors();
        showMainScreen();
    }

    private void showMainScreen() {
        settingsVisible = false;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(palette.background);
        applySystemBarPadding(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(16), dp(14), dp(16), dp(14));
        header.setBackground(roundRect(palette.surface, dp(0), palette.surface));
        header.setElevation(dp(4));
        root.addView(header);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(titleRow);

        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("zflogo", "drawable", getPackageName()));
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setPadding(dp(5), dp(5), dp(5), dp(5));
        logo.setBackground(roundRect(palette.surfaceHigh, dp(16), palette.outlineSoft));
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(58), dp(58));
        logoParams.setMargins(0, 0, dp(14), 0);
        titleRow.addView(logo, logoParams);

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleRow.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = text("Atık Bul ZFL", 25, palette.onSurface, true);
        titleBlock.addView(title);

        TextView subtitle = text("Zonguldak geri dönüşüm rehberi", 14, palette.onSurfaceVariant, false);
        titleBlock.addView(subtitle);

        Button settingsButton = iconButton("⚙", palette.surfaceHigh, palette.primary);
        settingsButton.setContentDescription("Ayarlar");
        settingsButton.setOnClickListener(v -> showSettingsScreen());
        titleRow.addView(settingsButton, new LinearLayout.LayoutParams(dp(48), dp(48)));

        header.addView(makeButtonRow(true));

        statusText = text("Konum izni bekleniyor; en yakın nokta için gerekli.", 13, palette.onSurfaceVariant, false);
        statusText.setPadding(0, dp(8), 0, 0);
        header.addView(statusText);

        FrameLayout mapFrame = new FrameLayout(this);
        root.addView(mapFrame, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        mapView = new MapView(this);
        mapView.setTileSource(palette.dark ? DARK_TILE_SOURCE : TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(ZONGULDAK_CENTER);
        mapFrame.addView(mapView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        addMapTools(mapFrame);
        addNearestPanel(mapFrame);

        setContentView(root);
        setupMapOverlays();
        refreshMarkers();
        ensureLocationPermission();
    }

    private void showSettingsScreen() {
        settingsVisible = true;
        if (mapView != null) {
            mapView.onPause();
        }

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(palette.background);
        applySystemBarPadding(page);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(12), dp(10), dp(16), dp(8));
        page.addView(toolbar);

        Button back = iconButton("‹", palette.surfaceHigh, palette.onSurface);
        back.setTextSize(28);
        back.setOnClickListener(v -> showMainScreen());
        toolbar.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView title = text("Ayarlar", 24, palette.onSurface, true);
        toolbar.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(8), dp(16), dp(24));
        scroll.addView(content);
        page.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout themeCard = card();
        themeCard.addView(text("Tema", 18, palette.onSurface, true));
        themeCard.addView(text("Uygulamayı açık veya koyu görünümde kullanın.", 14, palette.onSurfaceVariant, false));

        RadioGroup themeGroup = new RadioGroup(this);
        themeGroup.setOrientation(RadioGroup.VERTICAL);
        themeGroup.setPadding(0, dp(8), 0, 0);
        RadioButton light = radio("Açık tema", THEME_LIGHT);
        RadioButton dark = radio("Koyu tema", THEME_DARK);
        themeGroup.addView(light);
        themeGroup.addView(dark);
        themeGroup.check(isDarkTheme() ? dark.getId() : light.getId());
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            View selected = group.findViewById(checkedId);
            if (selected != null) {
                saveTheme((String) selected.getTag());
                palette = loadPalette();
                applyWindowColors();
                showSettingsScreen();
            }
        });
        themeCard.addView(themeGroup);
        content.addView(themeCard);

        LinearLayout projectCard = card();
        projectCard.addView(text("Proje", 18, palette.onSurface, true));
        projectCard.addView(text("Atık Bul ZFL, Zonguldak Fen Lisesi öğrencileri tarafından Liselerde Bilim Uygulamaları Yarışması için geliştirilmiştir.", 14, palette.onSurfaceVariant, false));
        Button github = actionButton("GitHub deposunu aç", palette.primary, palette.onPrimary);
        github.setCompoundDrawablesWithIntrinsicBounds(getResources().getIdentifier("ic_github", "drawable", getPackageName()), 0, 0, 0);
        github.setCompoundDrawablePadding(dp(10));
        github.setOnClickListener(v -> openUrl(GITHUB_URL));
        projectCard.addView(github);
        content.addView(projectCard);

        LinearLayout feedbackCard = card();
        feedbackCard.addView(text("Geri bildirim", 18, palette.onSurface, true));
        feedbackCard.addView(text("Yeni bir atık toplama konumu ekletmek, görüş bildirmek veya öneri paylaşmak için bize ulaşabilirsiniz.", 14, palette.onSurfaceVariant, false));
        Button feedback = actionButton("Geri bildirim gönder", palette.primary, palette.onPrimary);
        feedback.setOnClickListener(v -> sendFeedbackEmail());
        feedbackCard.addView(feedback);
        content.addView(feedbackCard);

        LinearLayout devCard = card();
        devCard.addView(text("Geliştiriciler", 18, palette.onSurface, true));
        String developers = "Ertuğrul Muhsin DANACI\nFurkan Yiğit ÖZDEN\nKayra Mehdi ARMAN\nEslem Sibel YILDIRIM\nToprak AÇIL\nEmir Kuzey GİDİCİ\nAyşe SAV\nPoyraz ŞEKERCİ";
        TextView devs = text(developers, 14, palette.onSurfaceVariant, false);
        devs.setLineSpacing(dp(2), 1.0f);
        devCard.addView(devs);
        content.addView(devCard);

        setContentView(page);
    }

    private RadioButton radio(String label, String value) {
        RadioButton radio = new RadioButton(this);
        radio.setText(label);
        radio.setTag(value);
        radio.setId(View.generateViewId());
        radio.setTextColor(palette.onSurface);
        radio.setTextSize(15);
        radio.setButtonTintList(android.content.res.ColorStateList.valueOf(palette.primary));
        radio.setPadding(0, dp(6), 0, dp(6));
        return radio;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        card.setBackground(roundRect(palette.surface, dp(22), palette.outlineSoft));
        card.setElevation(dp(3));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);
        return card;
    }

    private HorizontalScrollView makeButtonRow(boolean filters) {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, 0);
        scrollView.addView(row);

        if (filters) {
            filterButtons.clear();
            filterButtonCategories.clear();
            Button allButton = chip("Tümü", palette.primary, selectedCategory == null, true);
            allButton.setOnClickListener(v -> {
                selectedCategory = null;
                refreshMarkers();
                updateFilterButtons();
            });
            filterButtons.add(allButton);
            filterButtonCategories.add(null);
            row.addView(allButton);
        }

        for (WasteCategory category : WasteCategory.values()) {
            boolean selected = filters && selectedCategory == category;
            Button button = chip(filters ? category.label : "En yakın " + category.shortLabel.toLowerCase(new Locale("tr", "TR")),
                    filters ? category.color : palette.primary, selected, filters);
            if (filters) {
                button.setOnClickListener(v -> {
                    selectedCategory = category;
                    refreshMarkers();
                    updateFilterButtons();
                });
                filterButtons.add(button);
                filterButtonCategories.add(category);
            } else {
                button.setOnClickListener(v -> showNearest(category));
            }
            row.addView(button);
        }
        return scrollView;
    }

    private void updateFilterButtons() {
        for (int i = 0; i < filterButtons.size(); i++) {
            WasteCategory category = filterButtonCategories.get(i);
            int color = category == null ? palette.primary : category.color;
            boolean selected = category == selectedCategory;
            styleChip(filterButtons.get(i), color, selected, true);
        }
    }

    private void addNearestPanel(FrameLayout mapFrame) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(14));
        panel.setBackground(roundRect(alpha(palette.surface, palette.dark ? 236 : 248), dp(22), palette.outlineSoft));
        panel.setElevation(dp(10));

        panel.addView(text("En yakın toplama noktası", 14, palette.onSurface, true));
        panel.addView(makeButtonRow(false));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        params.setMargins(dp(12), 0, dp(12), dp(14));
        mapFrame.addView(panel, params);
    }

    private void addMapTools(FrameLayout mapFrame) {
        Button locate = actionButton("Konumum", palette.primary, palette.onPrimary);
        locate.setOnClickListener(v -> centerOnUser());
        FrameLayout.LayoutParams locateParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.RIGHT);
        locateParams.setMargins(0, dp(14), dp(14), 0);
        mapFrame.addView(locate, locateParams);
    }

    private void setupMapOverlays() {
        CompassOverlay compass = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        compass.enableCompass();
        mapView.getOverlays().add(compass);

        ScaleBarOverlay scaleBar = new ScaleBarOverlay(mapView);
        scaleBar.setAlignBottom(true);
        scaleBar.setAlignRight(false);
        scaleBar.setScaleBarOffset(dp(16), dp(132));
        mapView.getOverlays().add(scaleBar);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.setPersonIcon(makeUserDot());
        locationOverlay.setDirectionIcon(makeUserDot());
        mapView.getOverlays().add(locationOverlay);
    }

    private void refreshMarkers() {
        if (mapView == null) {
            return;
        }
        for (Marker marker : activeMarkers) {
            mapView.getOverlays().remove(marker);
        }
        activeMarkers.clear();

        for (RecyclingPoint point : points) {
            if (point.accepts.isEmpty()) {
                continue;
            }
            for (WasteCategory category : WasteCategory.values()) {
                if (!point.accepts.contains(category)) {
                    continue;
                }
                if (selectedCategory != null && selectedCategory != category) {
                    continue;
                }
                addMarker(point, category);
            }
        }
        mapView.invalidate();
    }

    private void addMarker(RecyclingPoint point, WasteCategory category) {
        Marker marker = new Marker(mapView);
        GeoPoint markerPoint = new GeoPoint(
                point.latitude + (selectedCategory == null ? category.latOffset : 0),
                point.longitude + (selectedCategory == null ? category.lonOffset : 0));
        marker.setPosition(markerPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(makePin(category.color, category.shortLabel.substring(0, 1)));
        marker.setTitle(point.name + " - " + category.label);
        marker.setSnippet(point.address + "\nTel: " + point.phone + "\nSorumlu: " + point.manager + "\nKabul: " + categoriesText(point)
                + (point.approximate ? "\nKonum adres bazlı yaklaşık girildi." : ""));
        mapView.getOverlays().add(marker);
        activeMarkers.add(marker);
    }

    private BitmapDrawable makePin(int color, String letter) {
        int width = dp(42);
        int height = dp(50);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(color);
        Path path = new Path();
        path.addCircle(width / 2f, dp(19), dp(16), Path.Direction.CW);
        path.moveTo(width / 2f - dp(8), dp(32));
        path.lineTo(width / 2f, dp(48));
        path.lineTo(width / 2f + dp(8), dp(32));
        path.close();
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(Color.WHITE);
        canvas.drawCircle(width / 2f, dp(19), dp(14), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(15));
        paint.setColor(Color.WHITE);
        canvas.drawText(letter, width / 2f, dp(25), paint);
        return new BitmapDrawable(getResources(), bitmap);
    }

    private Bitmap makeUserDot() {
        int size = dp(42);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0x332965F1);
        canvas.drawCircle(size / 2f, size / 2f, dp(18), paint);
        paint.setColor(0xFF2965F1);
        canvas.drawCircle(size / 2f, size / 2f, dp(9), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, dp(9), paint);
        return bitmap;
    }

    private String categoriesText(RecyclingPoint point) {
        List<String> names = new ArrayList<>();
        for (WasteCategory category : WasteCategory.values()) {
            if (point.accepts.contains(category)) {
                names.add(category.label);
            }
        }
        return android.text.TextUtils.join(", ", names);
    }

    private void showNearest(WasteCategory category) {
        if (userLocation == null) {
            Toast.makeText(this, "En yakın noktayı bulmak için konum izni ve konum bilgisi gerekli.", Toast.LENGTH_LONG).show();
            ensureLocationPermission();
            return;
        }

        RecyclingPoint nearest = null;
        double nearestMeters = Double.MAX_VALUE;
        for (RecyclingPoint point : points) {
            if (!point.accepts.contains(category)) {
                continue;
            }
            float[] result = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                    point.latitude, point.longitude, result);
            if (result[0] < nearestMeters) {
                nearestMeters = result[0];
                nearest = point;
            }
        }

        if (nearest == null) {
            Toast.makeText(this, category.label + " için uygun nokta bulunamadı.", Toast.LENGTH_LONG).show();
            return;
        }

        selectedCategory = category;
        refreshMarkers();
        GeoPoint target = new GeoPoint(nearest.latitude, nearest.longitude);
        mapView.getController().animateTo(target);
        mapView.getController().setZoom(16.0);
        statusText.setText(String.format(new Locale("tr", "TR"), "En yakın %s: %s (%.1f km)",
                category.shortLabel.toLowerCase(new Locale("tr", "TR")), nearest.name, nearestMeters / 1000.0));
    }

    private void centerOnUser() {
        GeoPoint overlayLocation = locationOverlay == null ? null : locationOverlay.getMyLocation();
        if (overlayLocation != null) {
            mapView.getController().animateTo(overlayLocation);
            mapView.getController().setZoom(16.0);
            statusText.setText("Harita konumunuza taşındı");
            return;
        }
        if (userLocation != null) {
            mapView.getController().animateTo(new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude()));
            mapView.getController().setZoom(16.0);
            statusText.setText("Harita konumunuza taşındı");
            return;
        }
        Toast.makeText(this, "Konum bilgisi henüz alınamadı.", Toast.LENGTH_LONG).show();
        ensureLocationPermission();
    }

    private void ensureLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocation();
            if (locationOverlay != null) {
                locationOverlay.enableMyLocation();
            }
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST);
        }
    }

    private void updateLocation() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return;
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        userLocation = bestLastKnownLocation(manager);
        if (userLocation != null) {
            statusText.setText("Konum alındı");
        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLocation = location;
                statusText.setText("Konum alındı");
            }
        };

        try {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
            } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null);
            }
        } catch (RuntimeException ignored) {
            statusText.setText(userLocation == null ? "Konum alınamadı" : "Konum alındı");
        }
    }

    private Location bestLastKnownLocation(LocationManager manager) {
        Location best = null;
        for (String provider : manager.getProviders(true)) {
            Location location = manager.getLastKnownLocation(provider);
            if (location != null && (best == null || location.getAccuracy() < best.getAccuracy())) {
                best = location;
            }
        }
        return best;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLocation();
            if (locationOverlay != null) {
                locationOverlay.enableMyLocation();
            }
        } else if (requestCode == LOCATION_REQUEST) {
            statusText.setText("Konum izni verilmedi");
        }
    }

    @Override
    public void onBackPressed() {
        if (settingsVisible) {
            showMainScreen();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
    }

    private Button chip(String text, int color, boolean selected, boolean compact) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(compact ? 13 : 12);
        button.setAllCaps(false);
        button.setMinHeight(dp(CHIP_HEIGHT_DP));
        button.setMinWidth(dp(compact ? 76 : 104));
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(CHIP_HEIGHT_DP));
        params.setMargins(0, 0, dp(10), 0);
        button.setLayoutParams(params);
        styleChip(button, color, selected, compact);
        return button;
    }

    private void styleChip(Button button, int color, boolean selected, boolean compact) {
        button.setTextColor(selected ? palette.onPrimary : palette.onSurface);
        button.setTextSize(compact ? 13 : 12);
        int fill = selected ? color : palette.surfaceHigh;
        int stroke = selected ? color : tone(color, palette.dark ? 0.42f : 0.76f);
        button.setBackground(roundRect(fill, dp(BUTTON_RADIUS_DP), stroke));
    }

    private Button actionButton(String text, int background, int foreground) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(14);
        button.setTextColor(foreground);
        button.setAllCaps(false);
        button.setMinHeight(dp(52));
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(18), 0, dp(18), 0);
        button.setBackground(roundRect(background, dp(BUTTON_RADIUS_DP), background));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52));
        params.setMargins(0, dp(12), 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button iconButton(String text, int background, int foreground) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(20);
        button.setTextColor(foreground);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(0, 0, 0, 0);
        button.setBackground(roundRect(background, dp(16), palette.outlineSoft));
        return button;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        text.setIncludeFontPadding(true);
        if (bold) {
            text.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return text;
    }

    private GradientDrawable roundRect(int color, int radius, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private GradientDrawable oval(int color, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int tone(int color, float amountToWhite) {
        if (palette.dark) {
            return Color.argb(255,
                    Math.max(0, (int) (Color.red(color) * amountToWhite)),
                    Math.max(0, (int) (Color.green(color) * amountToWhite)),
                    Math.max(0, (int) (Color.blue(color) * amountToWhite)));
        }
        return Color.argb(255,
                Math.min(255, (int) (Color.red(color) + (255 - Color.red(color)) * amountToWhite)),
                Math.min(255, (int) (Color.green(color) + (255 - Color.green(color)) * amountToWhite)),
                Math.min(255, (int) (Color.blue(color) + (255 - Color.blue(color)) * amountToWhite)));
    }

    private int alpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private Palette loadPalette() {
        boolean dark = isDarkTheme();
        if (dark) {
            return new Palette(0xFF0D1410, 0xFF171F1A, 0xFF223027, 0xFF7DD9A4, 0xFF00391D,
                    0xFFE6EEE7, 0xFFB8C8BC, 0xFF526159, 0xFF2D3A32, 0xFF0D1410,
                    0xFF0D1410, 0xAA07110B, true);
        }
        return new Palette(0xFFF5F8F2, 0xFFFFFFFF, 0xFFEAF3EA, 0xFF116B42, 0xFFFFFFFF,
                0xFF141D17, 0xFF536159, 0xFFB9C8BC, 0xFFDCE7DD, 0xFFF5F8F2,
                0xFFFFFFFF, 0x00000000, false);
    }

    private boolean isDarkTheme() {
        return THEME_DARK.equals(getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_THEME, THEME_LIGHT));
    }

    private void saveTheme(String theme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }

    private void applyWindowColors() {
        Window window = getWindow();
        window.setStatusBarColor(palette.statusBar);
        window.setNavigationBarColor(palette.navBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = palette.dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !palette.dark) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void applySystemBarPadding(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            view.setOnApplyWindowInsetsListener((v, insets) -> {
                int top = insets.getSystemWindowInsetTop();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(0, top, 0, bottom);
                return insets;
            });
        }
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void sendFeedbackEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + FEEDBACK_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Atık Bul ZFL geri bildirim");
        intent.putExtra(Intent.EXTRA_TEXT, "Merhaba Atık Bul ZFL ekibi,\n\n");
        try {
            startActivity(intent);
        } catch (RuntimeException exception) {
            Toast.makeText(this, "E-posta uygulaması bulunamadı: " + FEEDBACK_EMAIL, Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
