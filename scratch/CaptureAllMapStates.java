import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.swing.SwingUtilities;
import juanword.JuanWord;
import juanword.MapSelectPanel;
import juanword.Player;

public class CaptureAllMapStates {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord game = new JuanWord();
                Player juan = new Player("P001", "Juan");
                juan.unlockedMapIndex = 1; // Only Blue Building unlocked
                game.setCurrentPlayer(juan);
                game.setSize(960, 540);

                Field mpField = JuanWord.class.getDeclaredField("mapSelectPanel");
                mpField.setAccessible(true);
                MapSelectPanel mapPanel = (MapSelectPanel) mpField.get(game);
                mapPanel.setSize(960, 540);
                mapPanel.doLayout();

                Field animField = MapSelectPanel.class.getDeclaredField("animProgress");
                animField.setAccessible(true);
                animField.setFloat(mapPanel, 1.0f);

                // 1. Resting state (no hover)
                Field hoveredField = MapSelectPanel.class.getDeclaredField("hoveredLandmark");
                hoveredField.setAccessible(true);
                hoveredField.setInt(mapPanel, -1);

                BufferedImage imgResting = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gRest = imgResting.createGraphics();
                mapPanel.paint(gRest);
                gRest.dispose();
                ImageIO.write(imgResting, "png", new File("scratch/map_resting_no_yellow_circle.png"));
                System.out.println("Saved scratch/map_resting_no_yellow_circle.png");

                // 2. Hover Blue Building (Easy)
                hoveredField.setInt(mapPanel, MapSelectPanel.LANDMARK_BLUE_BUILDING);
                Field scaleField = MapSelectPanel.class.getDeclaredField("currentScale");
                scaleField.setAccessible(true);
                float[] scales = (float[]) scaleField.get(mapPanel);
                scales[MapSelectPanel.LANDMARK_BLUE_BUILDING] = 1.05f;
                scales[MapSelectPanel.LANDMARK_DURIAN_MUSEUM] = 1.0f;
                scales[MapSelectPanel.LANDMARK_SAN_PEDRO] = 1.0f;
                scales[MapSelectPanel.LANDMARK_MT_APO] = 1.0f;

                BufferedImage imgBB = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBB = imgBB.createGraphics();
                mapPanel.paint(gBB);
                gBB.dispose();
                ImageIO.write(imgBB, "png", new File("scratch/map_hover_blue_building_clean.png"));
                System.out.println("Saved scratch/map_hover_blue_building_clean.png");

                // 3. Hover Davao Museum (Medium)
                hoveredField.setInt(mapPanel, MapSelectPanel.LANDMARK_DURIAN_MUSEUM);
                scales[MapSelectPanel.LANDMARK_BLUE_BUILDING] = 1.0f;
                scales[MapSelectPanel.LANDMARK_DURIAN_MUSEUM] = 1.05f;

                BufferedImage imgDM = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gDM = imgDM.createGraphics();
                mapPanel.paint(gDM);
                gDM.dispose();
                ImageIO.write(imgDM, "png", new File("scratch/map_hover_davao_museum_clean.png"));
                System.out.println("Saved scratch/map_hover_davao_museum_clean.png");

                // 4. Hover San Pedro Cathedral (Hard)
                hoveredField.setInt(mapPanel, MapSelectPanel.LANDMARK_SAN_PEDRO);
                scales[MapSelectPanel.LANDMARK_DURIAN_MUSEUM] = 1.0f;
                scales[MapSelectPanel.LANDMARK_SAN_PEDRO] = 1.05f;

                BufferedImage imgSP = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gSP = imgSP.createGraphics();
                mapPanel.paint(gSP);
                gSP.dispose();
                ImageIO.write(imgSP, "png", new File("scratch/map_hover_san_pedro_clean.png"));
                System.out.println("Saved scratch/map_hover_san_pedro_clean.png");

                // 5. Hover Mt Apo (Extreme)
                hoveredField.setInt(mapPanel, MapSelectPanel.LANDMARK_MT_APO);
                scales[MapSelectPanel.LANDMARK_SAN_PEDRO] = 1.0f;
                scales[MapSelectPanel.LANDMARK_MT_APO] = 1.05f;

                BufferedImage imgApo = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gApo = imgApo.createGraphics();
                mapPanel.paint(gApo);
                gApo.dispose();
                ImageIO.write(imgApo, "png", new File("scratch/map_hover_mt_apo_clean.png"));
                System.out.println("Saved scratch/map_hover_mt_apo_clean.png");

                // Copy all to artifact directory
                String artDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";
                ImageIO.write(imgResting, "png", new File(artDir, "map_resting_no_yellow_circle.png"));
                ImageIO.write(imgBB, "png", new File(artDir, "map_hover_blue_building_clean.png"));
                ImageIO.write(imgDM, "png", new File(artDir, "map_hover_davao_museum_clean.png"));
                ImageIO.write(imgSP, "png", new File(artDir, "map_hover_san_pedro_clean.png"));
                ImageIO.write(imgApo, "png", new File(artDir, "map_hover_mt_apo_clean.png"));
                System.out.println("All images copied to artifact directory.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
