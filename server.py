#!/usr/bin/python3

import http.server
import socketserver
import os

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        # Modify the base directory to the 'data' subdirectory
        base_directory = os.path.join(os.getcwd(), 'data')
        
        # Get the default translated path
        path = super().translate_path(path)
        
        # Return the path relative to the new base directory
        return os.path.join(base_directory, os.path.relpath(path, os.getcwd()))

    def do_GET(self):
        # Construct the file path
        file_path = self.translate_path(self.path)

        # Check if the path is a file
        if os.path.isfile(file_path):
            # Serve the file
            self.send_response(200)
            self.send_header("Content-type", "text/plain; charset=utf-8")  # Content-Type header for plain text UTF-8
            self.end_headers()
            with open(file_path, 'rb') as file:
                self.copyfile(file, self.wfile)
        else:
            # File not found, send 404 response
            self.send_error(404, "File not found")

PORT = 8000  # Change this to the desired port

# Create the server
with socketserver.TCPServer(("", PORT), CustomHTTPRequestHandler) as httpd:
    print(f"Serving HTTP on port {PORT}")
    httpd.serve_forever()
