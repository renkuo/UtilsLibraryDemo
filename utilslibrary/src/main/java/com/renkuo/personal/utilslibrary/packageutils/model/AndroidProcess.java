/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.renkuo.personal.utilslibrary.packageutils.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.IOException;

public class AndroidProcess implements Parcelable {

  /**
   * Get the name of a running process.
   *
   * @param pid
   *     the process id.
   * @return the name of the process.
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  static String getProcessName(int pid) throws IOException {
    String cmdline = null;
    try {
      cmdline = ProcFile.readFile(String.format("/proc/%d/cmdline", pid)).trim();
    } catch (IOException ignored) {
    }
    if (TextUtils.isEmpty(cmdline)) {
      return Stat.get(pid).getComm();
    }
    return cmdline;
  }

  /** the process name */
  public final String name;

  /** the process id */
  public final int pid;

  /**
   * AndroidProcess constructor
   *
   * @param pid
   *     the process id
   * @throws IOException
   *     if /proc/[pid] does not exist or we don't have read access.
   */
  public AndroidProcess(int pid) throws IOException {
    this.pid = pid;
    this.name = getProcessName(pid);
  }

  public String read(String filename) throws IOException {
    return ProcFile.readFile(String.format("/proc/%d/%s", pid, filename));
  }


  public String attr_current() throws IOException {
    return read("attr/current");
  }


  public String cmdline() throws IOException {
    return read("cmdline");
  }


  public Cgroup cgroup() throws IOException {
    return Cgroup.get(pid);
  }


  public int oom_score() throws IOException {
    return Integer.parseInt(read("oom_score"));
  }


  public int oom_adj() throws IOException {
    return Integer.parseInt(read("oom_adj"));
  }


  public int oom_score_adj() throws IOException {
    return Integer.parseInt(read("oom_score_adj"));
  }


  public Stat stat() throws IOException {
    return Stat.get(pid);
  }

  public Statm statm() throws IOException {
    return Statm.get(pid);
  }


  public Status status() throws IOException {
    return Status.get(pid);
  }

  /**
   * The symbolic name corresponding to the location in the kernel where the process is sleeping.
   *
   * @return the contents of /proc/[pid]/wchan
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public String wchan() throws IOException {
    return read("wchan");
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.name);
    dest.writeInt(this.pid);
  }

  protected AndroidProcess(Parcel in) {
    this.name = in.readString();
    this.pid = in.readInt();
  }

  public static final Creator<AndroidProcess> CREATOR = new Creator<AndroidProcess>() {

    @Override
    public AndroidProcess createFromParcel(Parcel source) {
      return new AndroidProcess(source);
    }

    @Override
    public AndroidProcess[] newArray(int size) {
      return new AndroidProcess[size];
    }
  };

}
