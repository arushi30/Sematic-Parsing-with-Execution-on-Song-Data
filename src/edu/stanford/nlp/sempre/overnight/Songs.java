package edu.stanford.nlp.sempre.overnight;

import com.google.common.collect.Lists;
import fig.basic.*;
import edu.stanford.nlp.sempre.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;

/**
 * Functions for supporting a simple database.
 * This is very inefficient and it works only for small worlds.
 * Example applications: calendar, blocks world
 *
 * Types: DateValue, TimeValue, Value
 * All arguments are lists of Values.
 *
 */
public final class Songs {

  private Songs() { }


  // Declare entity, property, database objects
  private static Set<Value> entities;  // Keep track of all the entities
  private static Set<String> properties;  // Keep track of all the properties
  private static Map<String, String> propertyToType1, propertyToType2;  // types
  private static Map<Pair<Value, String>, List<Value>> database;  // Database consists of (e1, property, e2) triples

  public static int sizeofDB() {
    return database.size();
  }

  public static List<Value> lookupDB(Value e, String property) {
    createWorld();
    if (!entities.contains(e)) throw new RuntimeException("DB doesn't contain entity " + e);
    if (!properties.contains(property)) throw new RuntimeException("DB doesn't contain property " + property);
    System.out.println(Arrays.asList(database));
    List<Value> values = database.get(new Pair(e, property));
    if (values == null) return Collections.EMPTY_LIST;
    return values;
  }

  public static Boolean createWorld() {

    entities = new HashSet<>();
    properties = new HashSet<>();
    database = new HashMap<>();
    propertyToType1 = new HashMap<>();
    propertyToType2 = new HashMap<>();

    for (String line : IOUtils.readLinesHard("song_data/song_data.txt")) {
        String[] tokens = line.split("\t");
        String pred = tokens[0];
        Value e = makeValue(tokens[1]);
        if (tokens.length == 2) { // Unary
          insertDB(e, pred);
        } else if (tokens.length == 3) {  // Binary
          Value f;
          if (tokens[2].startsWith("en."))  // Named entity
            f = makeValue(tokens[2]);
          else
            f = Value.fromString(tokens[2]);  // Number
          insertDB(e, pred, f);
        } else {
          throw new RuntimeException("Unhandled: " + line);
        }
      }
    // System.out.println(Arrays.asList(propertyToType2)); // method 1
    // System.out.println(Collections.singletonList(propertyToType2));
    return true;
  }

  // Functions to insert entity-property or entity-property-entity tuples into the database
  private static void insertDB(Value e1, String property) {  // For unary properties
    insertDB(e1, property, new BooleanValue(true));
  }
  private static void insertDB(Value e1, String property, List<Value> e2s) {
    for (Value e2 : e2s) insertDB(e1, property, e2);
  }
  private static void insertDB(Value e1, String property, Value e2) {
    //LogInfo.logs("insertDB (%s, %s, %s)", e1, property, e2);
    entities.add(e1);
    properties.add(property);
    properties.add(reverse(property));
    entities.add(e2);
    MapUtils.addToList(database, new Pair(e1, property), e2);
    MapUtils.addToList(database, new Pair(e2, reverse(property)), e1);
    propertyToType1.put(property, getType(e1));
    propertyToType2.put(property, getType(e2));
    propertyToType1.put(reverse(property), getType(e2));
    propertyToType2.put(reverse(property), getType(e1));
  }

  private static Value toValue(Object obj) {
    if (obj instanceof Value) return (Value) obj;
    if (obj instanceof Boolean) return new BooleanValue((Boolean) obj);
    if (obj instanceof Integer) return new NumberValue((Integer) obj, "count");
    if (obj instanceof Double) return new NumberValue((Double) obj);
    if (obj instanceof String) return new StringValue((String) obj);
    if (obj instanceof List) {
      List<Value> list = Lists.newArrayList();
      for (Object elem : (List) obj)
        list.add(toValue(elem));
      return new ListValue(list);
    }
    throw new RuntimeException("Unhandled object: " + obj + " with class " + obj.getClass());
  }

  // Convert the Object to list value
  public static ListValue listValue(Object obj) {
    Value value = toValue(obj);
    if (value instanceof ListValue) {
      ListValue lv = (ListValue) value;
      Collections.sort(lv.values, new Value.ValueComparator());
      System.out.println(lv.values);
      return (ListValue) value;
    }
    return new ListValue(singleton(value));
  }

  // Create |numEntities| entities, the first few have ids.
  private static Value makeValue(String id) { return makeValue(id, extractType(id)); }
  private static Value makeValue(String id, String type) {
    Value e = new NameValue(id);
    if (!entities.contains(e)) {
      insertDB(e, "type", new NameValue(type));
    }
    return e;
  }

  // Return the type o the entity en.person.alice => en.person
  private static String extractType(String id) {
    int i = id.lastIndexOf('.');
    if (id.charAt(i + 1) == '_') { //to deal with /fb:en.lake._st_clair and such
      id = id.substring(0, i);
      i = id.lastIndexOf('.');
      return id.substring(0, i);
    }
    else return id.substring(0, i);
  }

  private static String getType(Value v) {
    if (v instanceof NumberValue) {
      String unit = ((NumberValue) v).unit;
      return unit + "_number"; // So we can quickly tell if something is a number or not
    } else if (v instanceof DateValue) {
      return "en.date";
    } else if (v instanceof TimeValue) {
      return "en.time";
    } else if (v instanceof BooleanValue) {
      return "en.boolean";
    } else if (v instanceof NameValue) {
      return extractType(((NameValue) v).id);
    } else if (v instanceof ListValue) {
      return getType(((ListValue) v).values.get(0));
    } else {
      throw new RuntimeException("Can't get type of value " + v);
    }
  }

  private static void checkTypeMatch(List<Value> l1, List<Value> l2) {
    for (Value o1 : l1) {
      for (Value o2 : l2) {
        if (!getType(o1).equals(getType(o2)))
          throw new RuntimeException("Intersecting objects with non-matching types, object 1: " +
                  o1 + ", object2: " + o2);
      }
    }
  }

  private static <T> boolean intersects(List<T> l1, List<T> l2) {
    //optimization
    if (l1.size() < l2.size()) {
      for (T o1 : l1) {
        if (l2.contains(o1))
          return true;
      }
    }
    else {
      for (T o2 : l2) {
        if (l1.contains(o2))
          return true;
      }
    }
    return false;
  }

  public static List<Value> singleton(Value value) { return Collections.singletonList(value); }

  public static String reverse(String property) {
    if (property.startsWith("!")) return property.substring(1);
    return "!" + property;
  }

  public static List<Value> concat(Value v1, Value v2) {
    return concat(singleton(v1), singleton(v2));
  }
  public static List<Value> concat(List<Value> l1, List<Value> l2) {
    checkTypeMatch(l1, l2);
    if (l1.equals(l2))  // Disallow 'alice' or 'alice'
      throw new RuntimeException("Cannot concatenate two copies of the same list: " + l1);
    List<Value> newList = new ArrayList<Value>();
    newList.addAll(l1);
    newList.addAll(l2);
    return newList;
  }

  private static void checkType1(String property, Value e1) {
    String type1 = propertyToType1.get(property);
    String type2 = "?";
    if (type1 == null)
      throw new RuntimeException("Property " + property + " has no type1");
    if (!getType(e1).equals(type1))
      throw new RuntimeException("Type check failed: " + property + " : (-> " + type1 + " " + type2 + ") doesn't match arg1 " + e1 + " : " + getType(e1));
  }

  private static void checkType2(String property, Value e2) {
    String type1 = "?";
    String type2 = propertyToType2.get(property);
    if (type2 == null)
      throw new RuntimeException("Property " + property + " has no type2");
    if (!getType(e2).equals(type2))
      throw new RuntimeException("Type check failed: " + property + " : (-> " + type1 + " " + type2 + ") doesn't match arg2 " + e2 + " : " + getType(e2));
  }

  private static void ensureNonnumericType2(String property) {
    createWorld();
    String type2 = propertyToType2.get(property);
    if (type2 == null)
      throw new RuntimeException("Property " + property + " has no type2");
    if (type2.endsWith("_number") || type2.equals("en.date") || type2.equals("en.time"))
      throw new RuntimeException("Property " + property + " has numeric type2, which is not allowed");
  }

  public static String ensureNumericProperty(String property) {
    createWorld();
    String type2 = propertyToType2.get(property);
    if (type2.endsWith("number") || type2.equals("en.date") || type2.equals("en.time")) {
      return property;
    }
    throw new RuntimeException("Property " + property + " has non-numeric type2, which is not allowed");
  }

  public static List<Value> ensureNumericEntity(Value value) {
    return ensureNumericEntity(singleton(value));
  }

  public static List<Value> ensureNumericEntity(List<Value> list) {
    createWorld();
    String type = getType(list.get(0));
    if (type.endsWith("number") || type.equals("en.date") || type.equals("en.time")) {
      return list;
    }
    throw new RuntimeException("List " + list + " is non-numeric, which is not allowed");
  }

  public static List<Value> filter(List<Value> entities, String property, String compare, List<Value> refValues) {
    List<Value> newEntities = new ArrayList<>();
    System.out.println("MADE IT INTO FILTER \n"); // method 1


    for (Value v : refValues)
      checkType2(property, v);

    for (Value obj : entities) {
      if (!(obj instanceof NameValue)) continue;
      NameValue e = (NameValue) obj;
      System.out.println("Made it to lookupDB\n"); // method 1
      List<Value> values = lookupDB(e, property);
      boolean match = false;

      checkType1(property, e);

      if (compare.equals("=")) {
        match = intersects(values, refValues);
      } else if (compare.equals("!=")) {
        match = !intersects(values, refValues);  // Note this is not the existential interpretation!
      } else if (compare.equals("<")) {
        match = getDegree(values, MIN) < getDegree(refValues, MAX);
      } else if (compare.equals(">")) {
        match = getDegree(values, MAX) > getDegree(refValues, MIN);
      } else if (compare.equals("<=")) {
        match = getDegree(values, MIN) <= getDegree(refValues, MAX);
      } else if (compare.equals(">=")) {
        match = getDegree(values, MAX) >= getDegree(refValues, MIN);
      }
      if (match) newEntities.add(e);
    }
    return newEntities;
  }

  public static List<Value> filter(List<Value> entities, NameValue property, String compare, Value refValue) {
    return filter(entities, property.id, compare, singleton(refValue));
  }

  public static List<Value> filter(List<Value> entities, String property) {  // Unary properties
    return filter(entities, property, "=", singleton(new BooleanValue(true)));
  }
  public static List<Value> filter(List<Value> entities, String property, String compare, Value refValue) {
    return filter(entities, property, compare, singleton(refValue));
  }


  private static double getDouble(Value v) {
    if (!(v instanceof NumberValue))
      throw new RuntimeException("Not a number: " + v);
    return ((NumberValue) v).value;
  }

  // Degree is used to compare (either take the max or min).
  private static double getDegree(List<Value> values, String mode) {
    double deg = Double.NaN;
    for (Value v : values) {
      double x = getDegree(v);
      if (Double.isNaN(deg) || (mode.equals(MAX) ? x > deg : x < deg))
        deg = x;
    }
    return deg;
  }
  private static double getDegree(Value value) {
    if (value instanceof TimeValue) {
      TimeValue timeValue = (TimeValue) value;
      return timeValue.hour;
    } else if (value instanceof DateValue) {
      DateValue dateValue = (DateValue) value;
      double dValue = 0;
      if (dateValue.year != -1) dValue += dateValue.year * 10000;
      if (dateValue.month != -1) dValue += dateValue.month * 100;
      if (dateValue.day != -1) dValue += dateValue.day;
      return dValue;
    } else if (value instanceof NumberValue) {
      return ((NumberValue) value).value;
    } else {
      throw new RuntimeException("Can't get degree from " + value);
    }
  }

  private static final String MIN = "min";
  private static final String MAX = "max";


  // Return the subset of entities that obtain the min/max value of property.
  public static List<Value> superlative(List<Value> entities, String mode, String property) {
    List<Value> bestEntities = null;
    double bestDegree = Double.NaN;

    for (Value e : entities) {
      double degree = getDegree(lookupDB(e, property), mode);
      checkType1(property, e);
      if (bestEntities == null || (mode.equals(MAX) ? degree > bestDegree : degree < bestDegree)) {
        bestEntities = new ArrayList<Value>();
        bestEntities.add(e);
        bestDegree = degree;
      } else if (degree == bestDegree) {
        bestEntities.add(e);
      }
    }
    return bestEntities;
  }

  // Return the subset of entities that obtain the most/least number of values of property that fall in restrictors.
  public static List<Value> countSuperlative(List<Value> entities, String mode, String property) {
    return countSuperlative(entities, mode, property, null);
  }

  //make sure lists are returned in a unique order
  public static String sortAndToString(Object obj) {
    if (obj instanceof List) {
      List<String> strList = new ArrayList<>();
      List list  = (List) obj;
      for (Object listObj: list)
        strList.add(listObj.toString());
      Collections.sort(strList);
      return strList.toString();
    }
    return obj.toString();
  }


  public static List<Value> countSuperlative(List<Value> entities, String mode, String property, List<Value> restrictors) {
    List<Value> bestEntities = null;
    double bestDegree = Double.NaN;

    if (restrictors != null) {
      for (Value v : restrictors)
        checkType2(property, v);
    }
    ensureNonnumericType2(property);

    for (Value e : entities) {
      List<Value> values = lookupDB(e, property);
      double degree = 0;
      for (Value v : values)
        if (restrictors == null || restrictors.contains(v))
          degree++;

      checkType1(property, e);

      if (bestEntities == null || (mode.equals(MAX) ? degree > bestDegree : degree < bestDegree)) {
        bestEntities = new ArrayList<Value>();
        bestEntities.add(e);
        bestDegree = degree;
      } else if (degree == bestDegree) {
        bestEntities.add(e);
      }
    }
    return bestEntities;
  }

  // Return subset of entities that have the number of values of property that meet the mode/threshold criteria (e.g. >= 3).
  public static List<Value> countComparative(List<Value> entities, String property, String mode, NumberValue thresholdValue) {
    return countComparative(entities, property, mode, thresholdValue, null);
  }
  public static List<Value> countComparative(List<Value> entities, String property, String mode, NumberValue thresholdValue, List<Value> restrictors) {
    List<Value> newEntities = new ArrayList<>();
    double threshold = getDouble(thresholdValue);

    if (restrictors != null) {
      for (Value v : restrictors)
        checkType2(property, v);
    }
    ensureNonnumericType2(property);

    for (Value e : entities) {
      List<Value> values = lookupDB(e, property);
      double degree = 0;
      for (Value v : values)
        if (restrictors == null || restrictors.contains(v))
          degree++;

      checkType1(property, e);

      switch (mode) {
        case "=": if (degree == threshold) newEntities.add(e); break;
        case "<": if (degree < threshold) newEntities.add(e); break;
        case ">": if (degree > threshold) newEntities.add(e); break;
        case "<=": if (degree <= threshold) newEntities.add(e); break;
        case ">=": if (degree >= threshold) newEntities.add(e); break;
        default: throw new RuntimeException("Illegal mode: " + mode);
      }
    }
    return newEntities;
  }

  // Return sum of values.
  public static List<Value> sum(List<Value> values) {
    double sum = 0;
    for (Value v : values)
      sum += getDouble(v);
    return Collections.singletonList(new NumberValue(sum));
  }

  // Return sum/mean/min/max of values.
  public static List<Value> aggregate(String mode, List<Value> values) {
    // Note: this is probably too strong to reject empty lists.
    if (values.size() == 0)
      throw new RuntimeException("Can't aggregate " + mode + " over empty list");
    double sum = 0;
    for (Value v : values) {
      // Note: we're leaving out dates and times, but sum/avg doesn't quite work with them anyway.
      if (!(v instanceof NumberValue))
        throw new RuntimeException("Can only aggregate over numbers, but got " + v);
      double x = getDouble(v);
      sum += x;
    }
    double result;
    switch (mode) {
      case "avg": result = sum / values.size(); break;
      case "sum": result = sum; break;
      default: throw new RuntimeException("Bad mode: " + mode);
    }
    return Collections.singletonList(new NumberValue(result, ((NumberValue) values.get(0)).unit));
  }

  public static List<Value> getProperty(Value inObject, String property) { return getProperty(singleton(inObject), property); }
  public static List<Value> getProperty(List<Value> inObjects, String property) {
    List<Value> outObjects = new ArrayList<>();
    Set<Value> outObjectsCache = new HashSet<>(); //optimization - run "contains" on set and not list
    for (Value obj : inObjects) {
      List<Value> values = lookupDB(obj, property);
      // checkType1(property, obj);
      for (Value v : values) {
        if (!outObjectsCache.contains(v)) {
          outObjects.add(v);
          outObjectsCache.add(v);
        }
      }
    }
    if (outObjects.size() == 0)
      throw new RuntimeException("The property " + property + " does not appear in any of the objects " + inObjects);
    return outObjects;
  }

  // Convert any $PHRASE (eg string) into the format of NameValue with an id of an entity of type song
  public static NameValue createSongEntity(String phrase) {
    String clean_phrase = "en.song." + phrase.replace(' ','_').toLowerCase();
    System.out.println(clean_phrase);
    return new NameValue(clean_phrase);
  }

  // Convert any $PHRASE (eg string) into the format of NameValue with an id of an entity of type song
  public static NameValue createArtistEntity(String phrase) {
    String clean_phrase = "en.artist." + phrase.replace(' ','_').toLowerCase();
    System.out.println(clean_phrase);
    return new NameValue(clean_phrase);
  }
}
