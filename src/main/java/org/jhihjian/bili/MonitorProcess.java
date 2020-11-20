package org.jhihjian.bili;

import org.jhihjian.bili.util.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Thread.sleep;

public class MonitorProcess {
    private  static final Logger logger= LoggerFactory.getLogger(MonitorProcess.class.getName());

    public static void main(String[] args) throws InterruptedException, IOException {
        SubtitleStore subtitleStore=new SubtitleStore();
        SubtitleReader subtitleReader=new SubtitleReader();
        Conf conf=new Conf();
        String dir_path=conf.getProperty("input_dir");
        String backup_dir_path=conf.getProperty("backup_dir");
        if(!new File(dir_path).exists()){
            Files.createDirectories(Paths.get( dir_path));
        }
        if(!new File(backup_dir_path).exists()){
            Files.createDirectories(Paths.get( backup_dir_path));
        }
        logger.info("input dir:{},backup dir:{}",dir_path,backup_dir_path);
        Set<Long> avSet=new HashSet<>( subtitleStore.queryAllAv());
        logger.info("already av num:{}",avSet.size());
        while(true){
            for(File file: new File(dir_path).listFiles()){
                logger.info("detect file:{}",file.getAbsolutePath());
                Long av;
                try {
                    av=Long.parseLong(file.getName().substring(0,file.getName().lastIndexOf('.')));
                }
                catch (Exception e){
                    continue;
                }
                if(avSet.contains(av)) {
                    continue;
                }
                String text = subtitleReader.getTotalText(file.getAbsolutePath());
                subtitleStore.storeText(av,text);
                avSet.add(av);
                try {
                    Files.move(Paths.get(file.getAbsolutePath()),Paths.get(backup_dir_path,file.getName()));
                }
                catch (Exception e){
                    logger.error("backup file:{] fail delete it",file.getAbsolutePath(),e);
                    Files.delete(Paths.get(file.getAbsolutePath()));
                }
            }
            sleep(1000);
        }
    }
}
