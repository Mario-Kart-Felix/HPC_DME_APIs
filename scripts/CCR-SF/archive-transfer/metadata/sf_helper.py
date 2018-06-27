import logging
import re
from datetime import datetime


class SFHelper(object):


    @staticmethod
    def get_pi_name(path, log = True):

        pi_names = {"Staudt": "Louis_Staudt", "Soppet": "Daniel_Soppet", "Schrump": "David_Schrump", "Shrump": "David_Schrump",
                    "Electron": "Electron_Kabebew", "Hager": "Gordon_Hager", "Hunter": "Kent_Hunter", "Mark_Raffeld_Brenda": "Mark_Raffeld",
                    "Jonathan_Keller_Sun": "Jonathan_Keller", "Nagao": "Keisuke_Nagao", "Bustin": "Michael_Bustin", "Restifo": "Nicholas_Restifo",
                    "Philipp_Oberdoerffer_Kim": "Philipp_Oberdoerffer", "Xin_Wei_Wang": "Xin_Wang", "Pommier": "Yves_Pommier", "Vinson": "Chuck_Vinson"}
        pi_name = 'CCRSF'

        if log is True:
            logging.info("Getting pi_name from path: " + path)


        if 'Undetermined' in path:
            pi_name = 'SF_Archive_flowcell_info'

        else:
            for element in pi_names:
                if element in path:
                    #Perform mapping using pi_names if match is found
                    pi_name = pi_names[element]
                    break


            if 'CCRSF' in pi_name:

                # derive pi name
                path_elements = (path.split("/")[0]).split("_")

                # Assumes that PI name is in the beginning, and last and first names are separated by an '_'

                if len(path_elements) > 4 and path_elements[3].isalpha() and path_elements[4].isdigit() and len(str(path_elements[4])):
                    # If the 4th is alpha, and 5th is a 5 digit number, then pick the first 2
                    pi_name = path_elements[0] + "_" + path_elements[1]
            #else:
                #if len(path_elements) > 2 and path_elements[2].isalpha() and path_elements[2] not in ['RAS', 'cegx', 'swift']:
                    # else if the first 3 are alpha pick 0 and 2
                    #pi_name = path_elements[0] + "_" + path_elements[2]
                #else:
                    #if len(path_elements) > 1 and path_elements[1].isalpha():
                        # else if the first 2 are alpha, pick 0 and 1
                        #pi_name = path_elements[0] + "_" + path_elements[1]
                    #else:
                        #pi_name = path_elements[0]


            #Assumes that PI name is in the beginning, and the format is FirstnameLastname
            #pi_name = re.sub(r'([A-Z])', r' \1', path_elements[0])

        if log is True:
            logging.info("pi_name from " + path + " is " + pi_name)
        return pi_name


    @staticmethod
    def get_contact_name(path):

        # derive pi name
        #path_elements = path.split("_")
        path_elements = (path.split("/")[0]).split("_")

        # Assumes the contact name follows the PI name separated from it by a '_',

        # the contact last and first names are separated by an '_'
        if len(path_elements) > 4 and path_elements[3].isalpha() and path_elements[4].isdigit() and len(str(path_elements[4])):
            contact_name = path_elements[2] + "_" + path_elements[3]
        else:
            contact_name = None

        # the contact name format is FirstnameLastname
        #if path_elements[1].isalpha():
            #contact_name = re.sub(r'([A-Z])', r'_\1', path_elements[1])
        #else:
            #contact_name = ""

        return contact_name



    @staticmethod
    def get_project_id(path, tarfile, log = True):

        if log is True:
            logging.info("Getting project_id from path: " + path)
        project_id = '00000'

        if 'Undetermined' in path:
            project_id =  SFHelper.get_run_name(tarfile)

        else:
            #path_elements = path.split("_")
            path_elements = (path.split("/")[0]).split("_")

            #The project_id is the first string containing only digits. If this string
            #is not a 5 digit number then use default project_id
            for element in path_elements:
                if element.isdigit():
                    if len(str(element)) is 5:
                        project_id = element
                    break

            #Assumes that PI and contact names are in the format 'FirstnameLastname'
            #project_id = path_elements[2]

        if log is True:
            logging.info("project_id from " + path + " is " + project_id)
        return project_id


    @staticmethod
    def get_project_name(path):

        if 'Undetermined' in path:
            project_name =  'Undetermined'

        else:
            # derive project name
            project_name = path.split("/")[0]

        return project_name


    @staticmethod
    def get_sample_name(path):
        logging.info("Getting sample_name from path: " + path)

        if 'Sample_' not in path:
            sample_name = 'Undetermined'

        else:
            # derive sample name
            sample_name = path.split("Sample_")[-1]

        logging.info("sample_name from " + path + " is " + sample_name)
        return sample_name


    @staticmethod
    def get_flowcell_id(tarfile, log = True):

        if log is True:
            logging.info("Getting flowcell_id from tarfile: " + tarfile)

        #Rule: After the last underscore in tar filename
        #flowcell_str = tarfile.split(".")[0].split("_")[-1]
        flowcell_str = tarfile.split(".")[0].split("_")[3]
        flowcell_id = flowcell_str[1:len(flowcell_str)]

        if log is True:
            logging.info("Flowcell_id from tarfile: " + tarfile + " is " + flowcell_id)

        return flowcell_id


    @staticmethod
    def get_run_date(tarfile):
        #Rule: String before the first underscore in tar filename - in the form YYMMDD
        #Change to MM/DD/YY
        run_date_str = tarfile.split(".")[0].split("_")[0]
        run_date = datetime.strptime(run_date_str, "%y%m%d").strftime("%m-%d-%y")
        return run_date


    @staticmethod
    def get_run_name(tarfile):
        #Rule: String before the '.tar' in the tar filename
        run_name = tarfile.split(".")[0]
        return run_name


    @staticmethod
    def get_sequencing_platform(tarfile):
        sequencing_platform = 'Unspecified'
        #Rule: First letter after the first '_' (i.e. 2nd column) in the tar filename
        sequencing_platform_code = tarfile.rstrip().split('_')[1][0]
        if(sequencing_platform_code == 'N'):
            sequencing_platform = 'NextSeq'
        elif (sequencing_platform_code == 'J' or sequencing_platform_code == 'D'):
            sequencing_platform = 'HiSeq'

        return sequencing_platform

    @staticmethod
    def get_sequencing_application_type(path):
        sequencing_application_type = 'Unspecified'
        if('RNA_' in path):
            sequencing_application_type = 'RNA'
        elif('Chip_' in path):
            sequencing_application_type = 'Chip'
        elif('exomelib' in path):
            sequencing_application_type = 'exomelib'


        return sequencing_application_type


