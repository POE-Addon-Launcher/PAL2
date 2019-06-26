using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Runtime.InteropServices;
using System.Net;
using System.Windows;
using System.IO;
using System.IO.Compression;

namespace PAL_Launcher
{
    class Program
    {
        [DllImport("kernel32.dll")]
        static extern IntPtr GetConsoleWindow();

        [DllImport("user32.dll")]
        static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);

        const int SW_HIDE = 0;
        const int SW_SHOW = 5;

        static void Main(string[] args)
        {
            // Check if the folder "new" exists
            // If new exists replace the current JAR with the one in "new"

            applyUpdate();
            
            
            Console.WriteLine("===========================");
            Console.WriteLine("Welcome to the PAL Launcher");
            Console.WriteLine("===========================");
            Console.WriteLine("");
            Console.WriteLine("Preparing to launch PAL2\n");

            Console.WriteLine("Detecting PAL2...");
            if (checkPAL())
            {
                Console.WriteLine("PAL2 found! Attempting to launch...");
            }
            else
            {
                Console.WriteLine("PAL2 not found!");
                Console.WriteLine("Downloading PAL2.jar!");
                FileDownloader.DownloadFile("https://github.com/POE-Addon-Launcher/PALRelease/raw/master/PAL2.jar", Path.Combine(Environment.CurrentDirectory, "PAL2.jar"), int.MaxValue);
            }

            Console.WriteLine("Searching for Java...");

            if (checkJava())
            {
                Thread t = new Thread(() =>
                {
                    var handle = GetConsoleWindow();
                    Console.WriteLine("Java found! Launching PAL2");
                    Process process = new Process();
                    process.StartInfo.FileName = "cmd.exe";
                    process.StartInfo.Arguments = "/c java -jar PAL2.jar";
                    process.StartInfo.UseShellExecute = false;
                    process.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
                    ShowWindow(handle, SW_HIDE);
                    process.Start();
                });
                t.Start();
            }
            else
            {
                // if ojdk folder exists, launch instead!
                string cdir = Path.Combine(Environment.CurrentDirectory, "jre");
                if (Directory.Exists(cdir))
                {
                    Console.WriteLine("Launching PAL2");
                    launchPAL2();
                }
                else
                {
                    Console.Clear();
                    Console.WriteLine("Java not found, downloading the Java Runtime...");

                    string jreZip = Path.Combine(Environment.CurrentDirectory, "jre.zip");

                    var success = FileDownloader.DownloadFile("https://github.com/POE-Addon-Launcher/PALRelease/releases/download/jre/jre.zip", jreZip, int.MaxValue);

                    if (success)
                    {
                        ZipStorer zip = ZipStorer.Open(jreZip, FileAccess.Read);
                        List<ZipStorer.ZipFileEntry> files = zip.ReadCentralDir();

                        Console.WriteLine("Extracting Zip Archive this may take some time...\nDO NOT CLOSE THE PROGRAM IT IS NOT STUCK!");

                        foreach (ZipStorer.ZipFileEntry entry in files)
                        {
                            zip.ExtractFile(entry, Path.Combine(Environment.CurrentDirectory, entry.FilenameInZip));
                        };
                        zip.Close();
                        Console.WriteLine("Deleting Archive...");
                        // TODO Delete zip
                        if (File.Exists(jreZip))
                        {
                            File.Delete(jreZip);
                        }

                        Console.WriteLine("Extracting Completed, launching PAL2");
                        launchPAL2();
                    }
                    else
                    {
                        Console.WriteLine("Download failed for some reason! Download java yourself instead.");
                    }
                }
            }
            
        }

        static void launchPAL2()
        {
            Thread t = new Thread(() =>
            {
                Console.WriteLine("Launching PAL2");
                var javaEXE = Path.Combine(Environment.CurrentDirectory, @"jre\bin\java.exe");
                string cmd = "/c ";
                cmd += javaEXE;
                cmd += " -jar PAL2.jar";
                var handle = GetConsoleWindow();
                Process process = new Process();
                process.StartInfo.FileName = "cmd.exe";
                process.StartInfo.Arguments = cmd;
                process.StartInfo.UseShellExecute = false;
                process.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
                ShowWindow(handle, SW_HIDE);
                process.Start();
            });
            t.Start();
        }

        static void killJava()
        {
            Process.Start("taskkill", "/F /IM java.exe");
            Process.Start("taskkill", "/F /IM javaw.exe");
            Console.WriteLine("Waiting for java to exit...");
            Thread.Sleep(3000);
        }

        /**
         * Overwrites PAL2.jar with the one in the "new" folder.  
         */
        static void applyUpdate()
        {
            var newest = Path.Combine(@Environment.CurrentDirectory, @"new\PAL2.jar");
            if (File.Exists(newest))
            {
                // Taskkill java & javaw
                killJava();

                string pal2jar = Path.Combine(Environment.CurrentDirectory, "PAL2.jar");
                if (checkPAL())
                {
                    File.Delete(pal2jar);
                }
                File.Move(newest, pal2jar);

                string pal2log = Path.Combine(Environment.CurrentDirectory, @"new\PAL_Logger.log");
                if (File.Exists(pal2log))
                {
                    File.Delete(pal2log);
                }


                string dir = Path.Combine(Environment.CurrentDirectory, @"new");
                if (Directory.Exists(dir))
                {
                    Directory.Delete(dir);
                }
            }
        }
        
        /**
         * Checks if PAL2.jar is in our root directory.
         */
        static bool checkPAL()
        {
            string pal2jar = Path.Combine(Environment.CurrentDirectory, "PAL2.jar");
            return File.Exists(pal2jar);
        }

        static Process process = new Process();
        static bool foundJava = false;

        /**
         * Checks if Java is installed.
         */
        static bool checkJava()
        {
            process.EnableRaisingEvents = true;
            process.OutputDataReceived += new DataReceivedEventHandler(process_OutputDataReceived);
            process.ErrorDataReceived += new DataReceivedEventHandler(process_ErrorDataReceived);
            process.Exited += new EventHandler(process_Exited);

            process.StartInfo.FileName = "cmd.exe";
            process.StartInfo.Arguments = "/c java -version";
            process.StartInfo.UseShellExecute = false;
            process.StartInfo.RedirectStandardError = true;
            process.StartInfo.RedirectStandardOutput = true;

            process.Start();
            process.BeginErrorReadLine();
            process.BeginOutputReadLine();
            
            process.WaitForExit();
            
            return foundJava;
        }
        
        static void process_Exited(object sender, EventArgs e)
        {
            //Console.WriteLine(string.Format("process exited with code {0}\n", process.ExitCode.ToString()));
        }

        static void process_ErrorDataReceived(object sender, DataReceivedEventArgs e)
        {
            if (e.Data != null)
            {
                if (e.Data.Contains("java version"))
                    foundJava = true;
            }
            
        }

        static void process_OutputDataReceived(object sender, DataReceivedEventArgs e)
        {
            //Console.WriteLine("Output: " + e.Data + "\n");
        }
    }
}
