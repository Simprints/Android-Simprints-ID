class Scanner:
    def __init__(self, scanner_id: str):
        self.scanner_id: str = scanner_id
        self.mac_address: str = Scanner.scanner_id_to_mac_address(scanner_id)

    @staticmethod
    def scanner_id_to_mac_address(scanner_id: str):
        hex_string = str(hex(int(scanner_id[2:8])))
        hex_string_digits = hex_string[2:len(hex_string)].zfill(5).upper()
        return f'F0:AC:D7:C{hex_string_digits[0]}:{hex_string_digits[1:3]}:{hex_string_digits[3:5]}'


class Device:
    def __init__(self, device_id: str, model: str):
        self.device_id: str = device_id
        self.model: str = model


class WifiNetwork:
    def __init__(self, ssid: str, password: str):
        self.ssid = ssid
        self.password = password
