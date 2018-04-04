package com.ghostwan.podtube.library.us.giga.get;

import android.support.annotation.Nullable;
import android.util.Log;
import com.coremedia.iso.boxes.Container;
import com.ghostwan.podtube.Util;
import com.google.gson.Gson;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;


public class DownloadManagerImpl implements DownloadManager {
    private static final String TAG = DownloadManagerImpl.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final String TEMP_FILE_NAME = "/merging_file";
    private final DownloadDataSource mDownloadDataSource;

    private final ArrayList<DownloadMission> mMissions = new ArrayList<DownloadMission>();
    private Collection<String> mSearchLocations;

    /**
     * Create a new instance
     *
     * @param searchLocations    the directories to search for unfinished downloads
     * @param downloadDataSource the data source for isFinished downloads
     */
    public DownloadManagerImpl(Collection<String> searchLocations, DownloadDataSource downloadDataSource) {
        mDownloadDataSource = downloadDataSource;
        mSearchLocations = searchLocations;
        loadMissions();
    }

    @Override
    public int startMission(String url, String location, String name, String type, int threads) {
        DownloadMission existingMission = getMissionByLocation(location, name);
        if (existingMission != null) {
            // Already downloaded or downloading
            if (existingMission.isFinished) {
                // Overwrite mission
                deleteMission(mMissions.indexOf(existingMission));
            } else {
                // Rename file (?)
                try {
                    name = generateUniqueName(location, name);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to generate unique name", e);
                    name = System.currentTimeMillis() + name;
                    Log.i(TAG, "Using " + name);
                }
            }
        }

        DownloadMission mission = new DownloadMission(name, url, location, type);
        mission.timestamp = System.currentTimeMillis();
        mission.threadCount = threads;
        mission.addListener(new MissionListener(mission));
        new Initializer(mission).start();
        return insertMission(mission);
    }

    @Override
    public void resumeMission(int i) {
        DownloadMission d = getMission(i);
        resumeMission(d);
    }

    @Override
    public void resumeMission(DownloadMission mission) {
        if (!mission.isRunning && mission.errCode == -1) {
            mission.start();
        }
    }

    @Override
    public void pauseMission(int i) {
        DownloadMission d = getMission(i);
        pauseMission(d);
    }

    @Override
    public void pauseMission(DownloadMission mission) {
        if (mission.isRunning) {
            mission.pause();
        }
    }

    @Override
    public void deleteMission(DownloadMission mission) {
        if (mission.isFinished) {
            mDownloadDataSource.deleteMission(mission);
        }
        mission.delete();
        mMissions.remove(mission);
    }

    @Override
    public void deleteMission(int i) {
        DownloadMission mission = getMission(i);
        deleteMission(mission);
    }


    @Override
    public void loadMissions() {
        mMissions.clear();
        loadFinishedMissions();
        for (String location : mSearchLocations) {
            loadMissions(location);
        }

    }


    /**
     * Loads isFinished missions from the data source
     */
    private void loadFinishedMissions() {
        List<DownloadMission> finishedMissions = mDownloadDataSource.loadMissions();
        if (finishedMissions == null) {
            finishedMissions = new ArrayList<>();
        }
        // Ensure its sorted
        Collections.sort(finishedMissions, (o1, o2) -> (int) (o1.timestamp - o2.timestamp));
        mMissions.ensureCapacity(mMissions.size() + finishedMissions.size());
        for (DownloadMission mission : finishedMissions) {
            File downloadedFile = mission.getDownloadedFile();
            if (!downloadedFile.isFile()) {
                if (DEBUG) {
                    Log.d(TAG, "downloaded file removed: " + downloadedFile.getAbsolutePath());
                }
                mDownloadDataSource.deleteMission(mission);
            } else {
                mission.length = downloadedFile.length();
                mission.isFinished = true;
                mission.isRunning = false;
                mMissions.add(mission);
            }
        }
    }

    private void loadMissions(String location) {

        File f = new File(location);

        if (f.exists() && f.isDirectory()) {
            File[] subs = f.listFiles();

            if (subs == null) {
                Log.e(TAG, "listFiles() returned null");
                return;
            }

            for (File sub : subs) {
                if (sub.isFile() && sub.getName().endsWith(".giga")) {
                    String str = Util.readFromFile(sub.getAbsolutePath());
                    if (str != null && !str.trim().equals("")) {

                        if (DEBUG) {
                            Log.d(TAG, "loading mission " + sub.getName());
                            Log.d(TAG, str);
                        }

                        DownloadMission mis = new Gson().fromJson(str, DownloadMission.class);

                        if (mis.isFinished) {
                            if (!sub.delete()) {
                                Log.w(TAG, "Unable to delete .giga file: " + sub.getPath());
                            }
                            continue;
                        }

                        mis.isRunning = false;
                        mis.recovered = true;
                        insertMission(mis);
                    }
                }
            }
        }
    }

    @Override
    public DownloadMission getMission(int i) {
        return mMissions.get(i);
    }

    @Override
    public int getCount() {
        return mMissions.size();
    }

    @Override
    public void mergeMission(DownloadMission mission) throws Exception {
        mission.isMerging = true;
        String inFilePathVideo=mission.getFileTokens()[0]+".mp4";
        String inFilePathAudio=mission.getFileTokens()[0]+".m4a";
        long currentMillis = System.currentTimeMillis();
        String outFile = mission.location + TEMP_FILE_NAME + currentMillis + ".mp4";
        mux(inFilePathVideo, inFilePathVideo, outFile);
        File inAudioFile = new File(inFilePathAudio);
        inAudioFile.delete();
        File inVideoFile = new File(inFilePathVideo);
        inVideoFile.delete();
        mission.type = Util.VIDEO_TYPE;
        mission.done = mission.length;
        mission.isMerging = false;
        mission.writeThisToFile();
//            File tempOutFile = new File(mission.location + TEMP_FILE_NAME + currentMillis + ".mp4");
//            tempOutFile.renameTo(inVideoFile);

    }

    public void mux(String videoFile, String audioFile, String outputFile) throws IOException {
        Movie video = new MovieCreator().build(videoFile);
        Movie audio = new MovieCreator().build(audioFile);

        Track audioTrack = audio.getTracks().get(0);
        video.addTrack(audioTrack);

        Container out = new DefaultMp4Builder().build(video);

        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
        out.writeContainer(byteBufferByteChannel);
        byteBufferByteChannel.close();
        fos.close();
    }

    private static class BufferedWritableFileByteChannel implements WritableByteChannel {
        //    private static final int BUFFER_CAPACITY = 1000000;
        private static final int BUFFER_CAPACITY = 10000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
            Log.e("Audio Video", "13");
        }

        @Override
        public int write(ByteBuffer inputBuffer) throws IOException {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                Log.e("Size ok ", "song size is ok");
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    Log.e("Size ok ", "song size is not okssss ok");
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() throws IOException {
            dumpToFile();
            isOpen = false;
        }

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int insertMission(DownloadMission mission) {
        int i = -1;

        DownloadMission m = null;

        if (mMissions.size() > 0) {
            do {
                m = mMissions.get(++i);
            } while (m.timestamp > mission.timestamp && i < mMissions.size() - 1);

            //if (i > 0) i--;
        } else {
            i = 0;
        }

        mMissions.add(i, mission);

        return i;
    }

    /**
     * Get a mission by its location and name
     *
     * @param location the location
     * @param name     the name
     * @return the mission or null if no such mission exists
     */
    private
    @Nullable
    DownloadMission getMissionByLocation(String location, String name) {
        for (DownloadMission mission : mMissions) {
            if (location.equals(mission.location) && name.equals(mission.name)) {
                return mission;
            }
        }
        return null;
    }

    /**
     * Splits the filename into name and extension
     * <p>
     * Dots are ignored if they appear: not at all, at the beginning of the file,
     * at the end of the file
     *
     * @param name the name to split
     * @return a string array with a length of 2 containing the name and the extension
     */
    private static String[] splitName(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex <= 0 || (dotIndex == name.length() - 1)) {
            return new String[]{name, ""};
        } else {
            return new String[]{name.substring(0, dotIndex), name.substring(dotIndex + 1)};
        }
    }

    /**
     * Generates a unique file name.
     * <p>
     * e.g. "myname (1).txt" if the name "myname.txt" exists.
     *
     * @param location the location (to check for existing files)
     * @param name     the name of the file
     * @return the unique file name
     * @throws IllegalArgumentException if the location is not a directory
     * @throws SecurityException        if the location is not readable
     */
    private static String generateUniqueName(String location, String name) {
        if (location == null) throw new NullPointerException("location is null");
        if (name == null) throw new NullPointerException("name is null");
        File destination = new File(location);
        if (!destination.isDirectory()) {
            throw new IllegalArgumentException("location is not a directory: " + location);
        }
        final String[] nameParts = splitName(name);
        String[] existingName = destination.list((dir, name1) -> name1.startsWith(nameParts[0]));
        Arrays.sort(existingName);
        String newName;
        int downloadIndex = 0;
        do {
            newName = nameParts[0] + " (" + downloadIndex + ")." + nameParts[1];
            ++downloadIndex;
            if (downloadIndex == 1000) {  // Probably an error on our side
                throw new RuntimeException("Too many existing files");
            }
        } while (Arrays.binarySearch(existingName, newName) >= 0);
        return newName;
    }

    private class Initializer extends Thread {
        private DownloadMission mission;

        public Initializer(DownloadMission mission) {
            this.mission = mission;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(mission.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                mission.length = conn.getContentLength();

                if (mission.length <= 0) {
                    mission.errCode = DownloadMission.ERROR_SERVER_UNSUPPORTED;
                    //mission.notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED);
                    return;
                }

                // Open again
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Range", "bytes=" + (mission.length - 10) + "-" + mission.length);

                if (conn.getResponseCode() != 206) {
                    // Fallback to single thread if no partial content support
                    mission.hasFallback = true;

                    if (DEBUG) {
                        Log.d(TAG, "falling back");
                    }
                }

                if (DEBUG) {
                    Log.d(TAG, "response = " + conn.getResponseCode());
                }

                mission.blocks = mission.length / BLOCK_SIZE;

                if (mission.threadCount > mission.blocks) {
                    mission.threadCount = (int) mission.blocks;
                }

                if (mission.threadCount <= 0) {
                    mission.threadCount = 1;
                }

                if (mission.blocks * BLOCK_SIZE < mission.length) {
                    mission.blocks++;
                }


                new File(mission.location).mkdirs();
                new File(mission.location + "/" + mission.name).createNewFile();
                RandomAccessFile af = new RandomAccessFile(mission.location + "/" + mission.name, "rw");
                af.setLength(mission.length);
                af.close();

                mission.start();
            } catch (Exception e) {
                // TODO Notify
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Waits for mission to finish to add it to the {@link #mDownloadDataSource}
     */
    private class MissionListener implements DownloadMission.MissionListener {
        private final DownloadMission mMission;

        private MissionListener(DownloadMission mission) {
            if (mission == null) throw new NullPointerException("mission is null");
            // Could the mission be passed in onFinish()?
            mMission = mission;
        }

        @Override
        public void onProgressUpdate(DownloadMission downloadMission, long done, long total) {
        }

        @Override
        public void onFinish(DownloadMission downloadMission) {
            mDownloadDataSource.addMission(mMission);
        }

        @Override
        public void onError(DownloadMission downloadMission, int errCode) {
        }
    }
}
