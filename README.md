# android                          boolean result = ftpObj.listFTPFiles("/", phoneNumber + ".txt");
                            if(result){
                                  ftpObj.downloadFTPFile(phoneNumber + ".txt", getApplicationContext().getFilesDir()+ "/Keys/");
                                  fileD = new File(getApplicationContext().getFilesDir(), "Keys/" + phoneNumber + ".txt");
                            }
                            StringBuilder text = new StringBuilder();

                            try {
                                BufferedReader br = new BufferedReader(new FileReader(fileD));
                                String line;

                                while ((line = br.readLine()) != null) {
                                    text.append(line);
                                    text.append('\n');
                                }
                                br.close();
                            }
                            catch (IOException e) {
                               e.printStackTrace();
                            }
                            String privateKey = Crypto.stripPrivateKeyHeaders(text.toString());
