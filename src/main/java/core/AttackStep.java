package core;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import static java.lang.Math.abs;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import vehicle.Data;
import vehicle.Information;

public class AttackStep {

   // This is the file that specifies all of the TTC probability distributions,
   // thus amounting to an attacker capability profile.
   public String                        ttcConfigFilePath       = "./target/generated-sources/attackerProfile.ttc";

   public final static double           oneSecond               = 0.00001157407;
   public final static double           infinity                = Double.MAX_VALUE;
   public double                        ttc                     = Double.MAX_VALUE;
   public Set<AttackStep>               expectedParents         = new HashSet<>();
   Set<AttackStep>                      visitedParents          = new HashSet<>();
   public static List<AttackStep>       allAttackSteps          = new ArrayList<>();
   public String                        assetName;
   public String                        assetClassName;
   private int                          explanationDepth        = 30;
   private boolean                      explained               = false;
   private static boolean               didReadDistributionFile = false;
   protected static Map<String, Double> ttcHashMap              = new HashMap<>();

   public AttackStep() {
      this("Anonymous");
   }

   public AttackStep(String name) {
      this.assetName = name;
      allAttackSteps.add(this);
      if (!didReadDistributionFile) {
         readDistribution();
      }
   }

   private void readDistribution() {
      try {
         BufferedReader in = new BufferedReader(new FileReader(ttcConfigFilePath));
         String line = "";
         while ((line = in.readLine()) != null) {
            addTtcHashMapRecord(line);
         }
         // System.out.println(ttcHashMap.toString());
         // System.out.println(ttcHashMap.get("OperatingSystem.maliciousConnectBypassEnpointProtection"));
         in.close();
         didReadDistributionFile = true;
      }
      catch (Exception e) {
         System.out.println(e.toString());
         System.exit(-1);
      }
   }

   public void addTtcHashMapRecord(String distributionString) {
      String parts[] = distributionString.split("=");
      String attackStepName = parts[0].trim();
      String distributionParts[] = parts[1].split("\\(");
      String distributionType = distributionParts[0].trim();
      String parameterString = "";
      List<Double> parameters = new ArrayList<>();
      if (Array.getLength(distributionParts) > 1) {
         parameterString = distributionParts[1].split("\\)")[0];
         if (!parameterString.contains(",")) {
            parameters.add(Double.parseDouble(parameterString.trim()));
         }
         else {
            if (Array.getLength(distributionParts) > 1) {
               parameterString = distributionParts[1].split("\\)")[0];
               if (parameterString.contains(",")) {
                  String tempParameterString[] = parameterString.split("\\,");
                  for (String pString : tempParameterString) {
                     parameters.add(Double.parseDouble(pString.trim()));
                  }
               }
            }
         }
      }
      addTtcHashMapRecord(attackStepName, distributionType, parameters);
   }

   public void customizeTtc(String distributionType, List<Double> parameters) {
      String attackStepName = this.assetClassName + "." + decapitalize(this.attackStepName());
      addTtcHashMapRecord(attackStepName, distributionType, parameters);
   }

   public void addTtcHashMapRecord(String attackStepName, String distributionType, List<Double> parameters) {
      if (distributionType.equals("Zero")) {
         ttcHashMap.put(attackStepName, oneSecond);
      }
      if (distributionType.equals("Infinity")) {
         ttcHashMap.put(attackStepName, infinity);
      }
      if (distributionType.equals("ExponentialDistribution")) {
         ttcHashMap.put(attackStepName, parameters.get(0));
      }
      if (distributionType.equals("GammaDistribution")) {
         ttcHashMap.put(attackStepName, parameters.get(0) * parameters.get(1));
      }
      if (distributionType.equals("UniformDistribution")) {
         ttcHashMap.put(attackStepName, (parameters.get(1) - parameters.get(0)) / 2);
      }
   }

   protected void setExpectedParents() {
   }

   public void updateChildren(Set<AttackStep> activeAttackSteps) {
   }

   public void updateTtc(AttackStep parent, double parentTtc, Set<AttackStep> activeAttackSteps) {
   }

   protected void addExpectedParent(AttackStep parent) {
      expectedParents.add(parent);
   }

   public double localTtc() {
      return oneSecond;
   }

   public String attackStepName() {
      return decapitalize(this.toString().substring(this.toString().lastIndexOf('$') + 1, this.toString().lastIndexOf('@')));
   }

   public String fullName() {
      return this.assetName + "." + attackStepName();
   }

   public Asset asset() {
      for (Asset asset : Asset.allAssets) {
         if (asset.name.equals(assetName)) {
            return asset;
         }
      }
      assertTrue("Asset name of " + fullName() + " does not correspond to any existing asset.", false);
      return null;
   }

   public void assertCompromisedInstantaneously() {
      if (ttc < 0.5) {
         System.out.println("+ " + fullName() + " was reached instantaneously as expected.");
         assertTrue(true);
      }
      else {
         System.out.println(fullName() + ".ttc was supposed to be small, but was " + Double.toString(ttc) + ".");
         explain();
         assertTrue(false);
      }
   }

   public void assertCompromisedWithEffort() {
      if (ttc >= 0.5 && ttc < 1000) {
         System.out.println("+ " + fullName() + " was reached in " + Double.toString(ttc) + " days, as expected.");
         assertTrue(true);
      }
      else {
         System.out.println(fullName() + ".ttc was supposed to be between 1 and 1000, but was " + Double.toString(ttc) + ".");
         explain();
         assertTrue(false);
      }
   }

   public void assertCompromisedInNDays(Double nDays) {
      if (ttc >= nDays && ttc < nDays + 1) {
         System.out.println("+ " + fullName() + " was reached in " + Double.toString(ttc) + " days, as expected.");
         assertTrue(true);
      }
      else {
         System.out.println(fullName() + ".ttc was supposed to be between " + nDays.toString() + " and " + Double.toString(nDays + 1) + ", but was " + Double.toString(ttc) + ".");
         explain();
         assertTrue(false);
      }
   }

   public void assertUncompromised() {
      if (ttc == Double.MAX_VALUE) {
         System.out.println("+ " + fullName() + " was not reached, as expected.");
         assertTrue(true);
      }
      else {
         System.out.println(fullName() + ".ttc was supposed to be infinite, but was " + Double.toString(ttc) + ".");
         System.out.println("\nExplaining compromise:");
         explainCompromise("", explanationDepth);
         assertTrue(false);
      }
   }

   public void assertCompromisedInstantaneouslyFrom(AttackStep expectedParent) {
      if (ttc - expectedParent.ttc < 0.5 && ttc - expectedParent.ttc > 0) {
         System.out.println("+ " + fullName() + " was reached instantaneously from " + expectedParent.fullName() + " as expected.");
         assertTrue(true);
      }
      else {
         System.out.println(
               fullName() + ".ttc (" + Double.toString(ttc) + ") was supposed to follow " + expectedParent.fullName() + ".ttc (" + Double.toString(expectedParent.ttc) + ") immediately, but didn't.");
         if (ttc - expectedParent.ttc < 0) {
            System.out.println("In fact, " + fullName() + " preceded " + expectedParent.fullName() + ".");
         }
         explain();
         assertTrue(false);
      }
   }
   
   public void assertUncompromisedFrom(AttackStep expectedParent) {
      if ((abs(ttc - expectedParent.ttc) == Double.MAX_VALUE) || (ttc == Double.MAX_VALUE && expectedParent.ttc == Double.MAX_VALUE)) {
         System.out.println("+ " + fullName() + " (" + Double.toString(ttc) + ")" + " was not reached from " + expectedParent.fullName() + " (" + Double.toString(expectedParent.ttc) + ")" + " as expected.");
         assertTrue(true);
      }
      else {
         System.out.println(fullName() + ".ttc was supposed to be infinite, but was " + Double.toString(ttc) + ", while " + expectedParent.fullName() + ".ttc was " + Double.toString(expectedParent.ttc) + ".");
         System.out.println("\nExplaining compromise:");
         explainCompromise("", explanationDepth);
         assertTrue(false);
      }
   }

   public void assertCompromisedWithEffortFrom(AttackStep expectedParent) {
      if (ttc - expectedParent.ttc >= 0.5 && ttc < Double.MAX_VALUE) {
         System.out.println("+ " + fullName() + " was reached in " + Double.toString(ttc - expectedParent.ttc) + " days from " + expectedParent.fullName() + " as expected.");
         assertTrue(true);
      }
      else {
         System.out.println(fullName() + ".ttc (" + Double.toString(ttc) + ") was supposed to follow " + expectedParent.getClass().getName() + ".ttc (" + Double.toString(expectedParent.ttc)
               + ") with some effort, but didn't.");
         if (ttc - expectedParent.ttc < 0) {
            System.out.println("In fact, " + fullName() + " preceded " + expectedParent.fullName() + ".");
         }
         explainCompromise("", explanationDepth);
         explainUncompromise("", explanationDepth);
         assertTrue(false);
      }
   }

   void reset() {
      ttc = Double.MAX_VALUE;
   }

   private void explainCompromise(String indent, int remainingExplanationSteps) {
      if (remainingExplanationSteps >= 0) {
         if (ttc != AttackStep.infinity) {
            System.out.print(indent + " reached " + fullName() + " [" + Double.toString(this.ttc) + "] (");
            if (this instanceof AttackStepMax) {
               System.out.print("AND");
            }
            if (this instanceof AttackStepMin) {
               System.out.print("OR");
            }
            System.out.print(") because ");
            // if (!explained) {
            explained = true;
            for (AttackStep parent : this.visitedParents) {
               System.out.print(" parent: " + parent.fullName() + " [" + Double.toString(parent.ttc) + "], ");
            }
            System.out.println("");
            for (AttackStep parent : this.visitedParents) {
               if (parent.ttc <= this.ttc) {
                  parent.explainCompromise(indent + "  ", remainingExplanationSteps - 1);
               }
            }
            // }
         }
      }
   }

   private void explainUncompromise(String indent, int remainingExplanationSteps) {
      if (remainingExplanationSteps >= 0) {
         if (ttc == AttackStep.infinity) {
            if (this instanceof AttackStepMax) {
               System.out.println(indent + " didn't reach " + fullName() + " [" + Double.toString(this.ttc) + "] (AND) because ");
               for (AttackStep parent : this.expectedParents) {
                  // System.out.println(indent + " " + parent.fullName() + " was
                  // not reached.");
                  parent.explainUncompromise(indent + "  ", remainingExplanationSteps - 1);
               }
            }
            if (this instanceof AttackStepMin) {
               System.out.println(indent + "  didn't reach " + fullName() + " [" + Double.toString(this.ttc) + "] (OR), because");
               if (visitedParents.isEmpty()) {
                  for (AttackStep parent : this.expectedParents) {
                     // System.out.println(indent + " " + parent.fullName() + "
                     // was not reached.");
                     parent.explainUncompromise(indent + "  ", remainingExplanationSteps - 1);
                  }
               }
            }
            if (this.expectedParents.isEmpty() && visitedParents.isEmpty()) {
               System.out.println(indent + "  parents were neither expected nor visited, so this step is unreachable.");
            }
            for (AttackStep parent : this.visitedParents) {
               parent.explainUncompromise(indent + "  ", remainingExplanationSteps - 1);
            }
         }
         else {
            System.out.println(indent + "  but did reach " + fullName() + " [" + Double.toString(this.ttc) + "].");
         }
      }
   }

   public void explain() {
      System.out.println("\nExplaining uncompromise:");
      explainUncompromise("", explanationDepth);
      System.out.println("\nExplaining compromise:");
      explainCompromise("", explanationDepth);
   }

   public void describe() {
      if (this.asset() instanceof Information) {
         for (Data datum : ((Information) this.asset()).data) {
            System.out.println(this.asset().name + " is stored as " + datum.name);
         }
      }

   }

   private String capitalize(final String line) {
      return Character.toUpperCase(line.charAt(0)) + line.substring(1);
   }

   private String decapitalize(final String line) {
      return Character.toLowerCase(line.charAt(0)) + line.substring(1);
   }

   public static AttackStep randomAttackStep(long randomSeed) {
      Random random = new Random(randomSeed);
      return allAttackSteps.get(random.nextInt(allAttackSteps.size()));
   }

   public static void printAllDefenseSettings() {
      List<String> defenseNames = new ArrayList<>();
      for (AttackStep attackStep : allAttackSteps) {
         if (attackStep.assetName.equals("Disable")) {
            defenseNames.add(attackStep.fullName());
         }
      }
      Collections.sort(defenseNames);
      for (String defenseName : defenseNames) {
         System.out.println(defenseName);
      }
   }

}
