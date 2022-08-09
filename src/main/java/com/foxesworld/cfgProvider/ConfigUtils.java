package com.foxesworld.cfgProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * @author AidenFox
 */
public abstract class ConfigUtils {

    /*INPUT*/
    protected static File classFullPath;
    protected static String cfgTemplate;
    /*INPUT*/

    private Boolean cached = false;
    private HashMap<String, String> cache;
    private InputStream input = null;

    protected ConfigUtils(File fullPath, String cfgTemplate) {
        ConfigUtils.classFullPath = fullPath;
        ConfigUtils.cfgTemplate = cfgTemplate;
        this.load();
    }

    private void load() {
        if (!classFullPath.exists()) {
            create();
            System.out.println("[CONFIG] Creating " + classFullPath);
        }
        if (cached) {
            cache = this.loadHashMap(classFullPath);
        }
    }

    private void create() {
        FileOutputStream output = null;
        try {
            classFullPath.getParentFile().mkdirs();
            output = new FileOutputStream(classFullPath);
            byte[] buf = new byte[8192];
            int length;
            while ((length = input.read(buf)) > 0) {
                output.write(buf, 0, length);
            }
        } catch (Exception e) {
        } finally {
            try {
                input.close();
            } catch (Exception ignored) {
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private HashMap<String, String> loadHashMap(File cfgFile) {
        HashMap<String, String> result = new HashMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(cfgFile));
            String line;
            while ((line = br.readLine()) != null) {
                if ((line.isEmpty()) || (line.startsWith("#")) || (!line.contains(": "))) {
                    continue;
                }
                String[] args = line.split(": ");
                if (args.length < 2) {
                    result.put(args[0], null);
                    continue;
                }
                result.put(args[0], args[1]);
            }
        } catch (IOException ex) {
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }
        return result;
    }

    private void flush(HashMap<Integer, String> newContents) {
        try {
            this.delFile(classFullPath);
            classFullPath.createNewFile();
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(classFullPath))) {
                for (int i = 1; i <= newContents.size(); i++) {
                    String line = newContents.get(i);
                    if (line == null || line.split(": ").length == 1) {
                        writer.append("");
                        continue;
                    }
                    writer.append(line);
                    writer.append("\n");
                }
                writer.flush();
            }
            if (cached) {
                load();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    private HashMap<Integer, String> getAllFileContents() {
        HashMap<Integer, String> result = new HashMap<>();
        BufferedReader br = null;
        Integer i = 1;
        try {
            br = new BufferedReader(new FileReader(classFullPath));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    result.put(i, null);
                    i++;
                    continue;
                }

                result.put(i, line);
                i++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }

        return result;
    }

    protected void put(String property, Object obj) {
        HashMap<Integer, String> contents = this.getAllFileContents();
        contents.put(contents.size() + 1, property + ": " + obj.toString());
        flush(contents);
    }

    protected Integer getLineCount() {
        HashMap<Integer, String> contents = getAllFileContents();
        return contents.size();
    }

    /* GETTING PROPERTY */

    protected Boolean checkProperty(String key) {
        String check;
        try {
            if (cached) {
                check = cache.get(key);
            } else {
                HashMap<String, String> contents = this.loadHashMap(classFullPath);
                check = contents.get(key);
            }
            if (check != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    protected String getPropertyString(String property) {
        try {
            if (cached) {
                return cache.get(property);
            } else {
                HashMap<String, String> contents = loadHashMap(classFullPath);
                return contents.get(property);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    protected Integer getPropertyInteger(String property) {
        try {
            if (this.cached) {
                return Integer.parseInt(cache.get(property));
            } else {
                HashMap<String, String> contents = loadHashMap(classFullPath);
                return Integer.parseInt(contents.get(property));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    protected Boolean getPropertyBoolean(String property) {
        try {
            String result;
            if (this.cached) {
                result = this.cache.get(property);
            } else {
                HashMap<String, String> contents = this.loadHashMap(classFullPath);
                result = contents.get(property);
            }
            if (result != null && result.equalsIgnoreCase("true")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    /*WIP*/
    protected Double getPropertyDouble(String property) {
        try {
            String result;
            if (cached) {
                result = cache.get(property);
            } else {
                HashMap<String, String> contents = this.loadHashMap(classFullPath);
                result = contents.get(property);
            }
            if (!result.contains("")) {
                result += ".0";
            }
            return Double.parseDouble(result);
        } catch (Exception ignored) {
        }
        return null;
    }
}
