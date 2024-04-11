package br.com.archbase.generator.code;

public class TypeScriptServiceGenerator {


    public static String generateTypeScriptClass(Class<?> entityClass, String version, String iocTypesPath) {
        String name = entityClass.getSimpleName().replace("Dto","");
        String serviceName = name+ "RemoteService";
        String dtoName = entityClass.getSimpleName();
        String endpoint = "api/" + version + "/" + unCapitalize(name);
        String isNewProperty = "isNew" + name;

        return "import { inject, injectable } from 'inversify'\n"
                + "import { API_TYPE } from '" + iocTypesPath + "'\n"
                + "import { ArchbaseRemoteApiClient, ArchbaseRemoteApiService } from 'archbase-react'\n"
                + "import { " + dtoName + " } from '@/domain/" + dtoName + "'\n\n"
                + "@injectable()\n"
                + "export class " + serviceName + " extends ArchbaseRemoteApiService<" + dtoName + ", string> {\n"
                + "  constructor(@inject(API_TYPE.ApiClient) client: ArchbaseRemoteApiClient) {\n"
                + "    super(client)\n"
                + "  }\n\n"
                + "  protected transform(entity: " + dtoName + "): " + dtoName + " {\n"
                + "    return new " + dtoName + "(entity)\n"
                + "  }\n\n"
                + "  protected getEndpoint(): string {\n"
                + "    return '" + endpoint + "'\n"
                + "  }\n\n"
                + "  protected configureHeaders(): Record<string, string> {\n"
                + "    return {};\n"
                + "  }\n\n"
                + "  public getId(entity: " + dtoName + "): string {\n"
                + "    return entity.id;\n"
                + "  }\n\n"
                + "  async save<R>(entity: " + dtoName + "): Promise<R> {\n"
                + "    if (entity." + isNewProperty + ") {\n"
                + "      return this.client.post<" + dtoName + ", R>(this.getEndpoint(), entity, this.configureHeaders(), false);\n"
                + "    }\n"
                + "    return this.client.put<" + dtoName + ", R>(`${this.getEndpoint()}/${entity.id}`, entity, this.configureHeaders(), false);\n"
                + "  }\n\n"
                + "  async delete<R>(id: string): Promise<R> {\n"
                + "    return this.client.delete<R>(`${this.getEndpoint()}/${id}`, this.configureHeaders(), false);\n"
                + "  }\n\n"
                + "  async findOne(id: string): Promise<" + dtoName + "> {\n"
                + "    return new Promise<" + dtoName + ">(async (resolve, reject) => {\n"
                + "      try {\n"
                + "        const result = await this.client.get<" + dtoName + ">(`${this.getEndpoint()}/${id}`, this.configureHeaders(), false);\n"
                + "        resolve(this.transform(result));\n"
                + "      } catch (error) {\n"
                + "        reject(error);\n"
                + "      }\n"
                + "    });\n"
                + "  }\n"
                + "}\n";
    }


    private static String unCapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}
