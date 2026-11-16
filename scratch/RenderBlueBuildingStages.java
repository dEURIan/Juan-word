package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import juanword.*;

public class RenderBlueBuildingStages {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                DataManager dm = DataManager.getInstance();
                Player player = new Player("P_VISUAL", "Juan");
                player.currentHealth = 100;
                dm.savePlayer(player);

                String[] stages = {"Stage 1 - Student", "Stage 2 - Security Guard", "Stage 3 - Dizon Boss"};
                int[] stageNums = {1, 2, 3};
                String[] outNames = {"blue_building_stage1_student.png", "blue_building_stage2_securityguard.png", "blue_building_stage3_dizon_boss.png"};

                for (int i = 0; i < 3; i++) {
                    int st = stageNums[i];
                    BattlePanel panel = new BattlePanel(app, dm);
                    panel.setupBattle(player, "Blue Building", st, "Sayon");
                    panel.dismissTitleCardForTesting();

                    JFrame frame = new JFrame("Render " + stages[i]);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.getContentPane().setPreferredSize(new Dimension(960, 540));
                    frame.getContentPane().add(panel);
                    frame.pack();
                    panel.setSize(960, 540);
                    panel.updateResponsiveLayout(960, 540);
                    frame.validate();
                    panel.doLayout();

                    BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = img.createGraphics();
                    panel.paint(g2);
                    g2.dispose();

                    File outFile = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/" + outNames[i]);
                    ImageIO.write(img, "png", outFile);
                    System.out.println("Saved " + outFile.getAbsolutePath());

                    frame.dispose();
                }

                // Create combined trio image
                BufferedImage s1 = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/blue_building_stage1_student.png"));
                BufferedImage s2 = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/blue_building_stage2_securityguard.png"));
                BufferedImage s3 = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/blue_building_stage3_dizon_boss.png"));

                int tw = 480;
                int th = 270;
                BufferedImage trio = new BufferedImage(tw * 3, th, BufferedImage.TYPE_INT_RGB);
                Graphics2D gt = trio.createGraphics();
                gt.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gt.drawImage(s1, 0, 0, tw, th, null);
                gt.drawImage(s2, tw, 0, tw, th, null);
                gt.drawImage(s3, tw * 2, 0, tw, th, null);
                gt.dispose();

                File trioFile = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/blue_building_stages_trio.png");
                ImageIO.write(trio, "png", trioFile);
                System.out.println("Saved " + trioFile.getAbsolutePath());

                dm.deletePlayer("P_VISUAL");
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
